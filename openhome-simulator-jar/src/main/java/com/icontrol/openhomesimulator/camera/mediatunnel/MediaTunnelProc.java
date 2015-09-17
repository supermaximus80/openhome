package com.icontrol.openhomesimulator.camera.mediatunnel;

import com.icontrol.openhome.data.*;
import com.icontrol.openhomesimulator.camera.resources.SystemResource;
import com.icontrol.openhomesimulator.gateway.simplerelay.RelayThreadStatusCallBack;
import com.icontrol.openhomesimulator.camera.CameraSimulatorFactory;
import com.icontrol.openhomesimulator.camera.History;
import com.icontrol.openhomesimulator.camera.RtspURL;
import com.icontrol.rest.framework.RestClient;
import com.icontrol.rest.framework.RestConstants;
import org.slf4j.Logger;
import com.icontrol.openhomesimulator.util.AuthenticationInfo;
import com.icontrol.openhomesimulator.util.URLConnectionHelper;
import com.icontrol.openhomesimulator.util.Utilities;
import com.icontrol.ohsimsolver.ResponseStatusFactory;
import com.icontrol.ohsimsolver.Wrappers;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

public class MediaTunnelProc extends TimerTask implements RelayThreadStatusCallBack {

    AuthenticationInfo auth;
    SSLSocketFactory factory;
    Logger log;
    String id;
    URL gwUrl;
    String failureUrl;
    MediaTunnelStatusInterf statusCallback;
    long startTime;
    boolean isLive;
    RtspURL rtspSourceURL;
    int curRetryCount;
    Socket rtspSocket ;
    InputStream rtspInputStream ;
    OutputStream rtspOutputStream;
    long maxMediaTunnelReadyWait;
    long maxBackOffRetries ;
    long maxBackOffMS;

    public MediaTunnelProc(AuthenticationInfo auth, SSLSocketFactory factory, Logger log) {
        this.auth = auth;
        this.factory = factory;
        this.log = log;
        this.id = null;
        this.gwUrl = null;
        this.failureUrl = null;
        this.statusCallback = null;
        this.startTime = System.currentTimeMillis();
        isLive = true;
        curRetryCount = 0;
        rtspSocket = null ;
        rtspInputStream = null;
        rtspOutputStream = null;
        // default timer values
        maxMediaTunnelReadyWait = SystemResource.ConfigurationDataResource.TimersResource.getMaxMediaTunnelReadyWait();
        maxBackOffRetries = 20;
        maxBackOffMS = 10*1000;
    }

    public ResponseStatusFactory.STATUSCODE start(String id, String gwUrl, String failureUrl, MediaTunnelStatusInterf statusCallback) throws MalformedURLException, IOException {
        this.id = id;
        this.gwUrl = new URL(gwUrl);
        this.failureUrl = failureUrl;
        this.startTime = System.currentTimeMillis();
        this.statusCallback = statusCallback;
        this.rtspSourceURL = CameraSimulatorFactory.getInstance().getRtspSourceURL("640x480");
        curRetryCount = 0;

        if (rtspSourceURL==null)
            throw new IOException("NULL rtspSourceURL");

        // validate gateway url
        log.debug("MediaTunnel create id="+id+" GatewayURL="+gwUrl+" failureURL="+failureUrl+" rtspSourceURL="+rtspSourceURL.toString());

        // connect to camera first before responding
        try {
            // create connection to rtsp source
            InetSocketAddress sockeAddr = new InetSocketAddress(rtspSourceURL.getHost(), rtspSourceURL.getPort());
            rtspSocket = new Socket();
            rtspSocket.connect(sockeAddr, 2000);
            rtspOutputStream = rtspSocket.getOutputStream();
            rtspInputStream = rtspSocket.getInputStream();
        } catch (IOException ex) {
            log.error("MediaTunnel.start socket to rtsp source caught "+ex);
            return ResponseStatusFactory.STATUSCODE.DEVICE_ERROR;
        }


        // start upload task in a separate thread
        Timer execTimer = new Timer();
        execTimer.schedule(this, 100);
        return ResponseStatusFactory.STATUSCODE.OK;
    }

    public String getSessionID() {
        return id;
    }

    public String getTransportSecurity() {
        if (gwUrl != null && gwUrl.getProtocol().startsWith("https"))
            return "TLS";
        else
            return "NONE";
    }

    public int getElapsedTime() {
        return (int)((System.currentTimeMillis()-startTime)/1000);
    }

    /*
        TimerTask run
     */
    public void run() {
        log.debug("MediaTunnelProc.run id="+id+" gatewayURL="+gwUrl.toString()+" failureURL="+failureUrl+" rtspSource="+rtspSourceURL.toString());

        int code = 0;
        long maxBackOff = maxBackOffRetries;
        try {
            while (isLive) {
                code = doTunnel(gwUrl.getHost(), gwUrl.getPort(), maxMediaTunnelReadyWait) ;
                // continue to reconnect until shutdown
                if (++curRetryCount >= maxBackOffRetries) {
                    log.error("Reached max backoff retries. Exit");
                    break;
                }
                long backoff = maxBackOffMS;
                try {
                    Thread.sleep(backoff);
                } catch (Exception ex) {}
            }
        } catch (Exception ex) {
            log.error("Upload failed. caught "+ex);
            code = -1;
        }

        if (code != 200 && failureUrl != null) {
            try {
                log.error("MediaTunnelProc unsuccessful code="+code+" Post to failure url = "+failureUrl);
                createFailure("POST", failureUrl, id) ;
            } catch (Exception ex) {
                log.error("uploadFailure caught "+ex);
            }
        }

    }

    private int doTunnel(String gatewayHost, int gatewayPort, long maxMediaTunnelReadyWait) {
        log.debug("Starting media tunnel id=" + id +" retries="+curRetryCount);
        // connect to relay server first, wait until 1st received packet to build connection to rtsp source
        Socket gwSocket = null;
        InputStream gwInputStream = null;
        OutputStream gwOutputStream=null;
        MediaTunnelThread gw2rtsp=null, rtsp2gw=null;
        try {
            InetSocketAddress sockeAddr = new InetSocketAddress(gatewayHost, gatewayPort);

            if ("TLS".equals(getTransportSecurity())) {
                gwSocket = Utilities.getClientSideSSLSocketFactory().createSocket();
                String[] suites = ((SSLSocket)gwSocket).getSupportedCipherSuites();
                ((SSLSocket)gwSocket).setEnabledCipherSuites(suites);
            } else {
                gwSocket = new Socket();
            }

            gwSocket.connect(sockeAddr, 2000);

            if ("TLS".equals(getTransportSecurity())) {
                // This is the SSL handshake being performed, it's separate from the TCP
                // connection, and happens afterwards.
                ((SSLSocket)gwSocket).startHandshake();
                log.debug("doTunnel SSL socket connected to "+gatewayHost+":"+gatewayPort);
            } else {
                log.debug("doTunnel socket connected to "+gatewayHost+":"+gatewayPort);
            }

            // set socket attributes
            gwSocket.setSoTimeout((int)maxMediaTunnelReadyWait);
            // write request line to gateway
            gwOutputStream = gwSocket.getOutputStream();
            gwInputStream = gwSocket.getInputStream();
            String reqLine = "POST "+gwUrl.getPath() + " HTTP/1.1\r\n\r\n";         // must end with \r\n
            //log.debug("doTunnel sending reqLine="+reqLine);
            gwOutputStream.write(reqLine.getBytes(), 0, reqLine.length());
            gwOutputStream.flush();

            // create translate RTSP obj
            TranslateRTSP translateRTSP = new TranslateRTSP(rtspSourceURL.toString(), log);

            // create tunnel passthru
            // user --> camera
            gw2rtsp = new MediaTunnelThread("gw->rtsp", this, gwInputStream, rtspOutputStream, true, translateRTSP, log);
            // camera --> user
            rtsp2gw = new MediaTunnelThread("rtsp->gw", this, rtspInputStream, gwOutputStream, false, null, log);

            rtspSocket.setSoTimeout(0);
            gwSocket.setSoTimeout(0);

            rtsp2gw.getTaskThread().start();
            gw2rtsp.getTaskThread().run();
            log.debug("Finished streaming camera tunnel id=" + id);
        } catch(UnknownHostException unknownHost) {
            log.error("doTunnel unknownHost error "+unknownHost, unknownHost);
            return 500;
        } catch (java.net.SocketException ex) {
            log.error("doTunnel caught "+ex);
            return 500;
        } catch(Exception ex){
            log.error("doTunnel caught "+ex, ex);
            return 500;
        } finally {
            try {
                if (gwInputStream != null)
                    gwInputStream.close();
                if (gwOutputStream != null)
                    gwOutputStream.close();
                if (rtspInputStream != null)
                    rtspInputStream.close();
                if (rtspOutputStream != null)
                    rtspOutputStream.close();
                if (gwSocket != null)
                    gwSocket.close();
                if (rtspSocket != null)
                    rtspSocket.close();
            } catch (Exception ex) {
                log.error("doTunnel inner caught "+ex);
            }
        }

        return 200;
    }

    private int createFailure(String method, String uriStr, String id) throws Exception {
        HttpURLConnection conn = null;
        DataOutputStream wr = null;
        try {
            // inform call back
            if (statusCallback != null)
                statusCallback.createdFailed(id);

            // create CreateMediaTunnelFailure XML object
            CreateMediaTunnelFailure failure = new CreateMediaTunnelFailure();
            // id
            failure.setId(Wrappers.createStringCap(id));
            // dateTime
            GregorianCalendar gcal = new GregorianCalendar();
            gcal.setTime(new Date());
            XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
            DateTimeCap dateTime = new DateTimeCap();
            dateTime.setValue(xgcal);
            failure.setDateTime(dateTime);
            String xmlBody = RestClient.toString(failure, RestConstants.ContentType.TEXT_XML);

            // http connection
            HashMap query = new HashMap();
            conn = new URLConnectionHelper(auth, factory).getConnection(uriStr, query);

            // set method
            conn.setRequestMethod(method);
            conn.setDoInput(true);

            // set request headers
            if (method.equals("PUT") || method.equals("POST")) {
                conn.setRequestProperty("Content-Language", "en-US");

                conn.setUseCaches(false);
                conn.setDoOutput(true);

                //Send request
                if (xmlBody != null && xmlBody.length() > 0) {
                    conn.setRequestProperty("Content-Type", "application/xml");
                    conn.setRequestProperty("Content-Length", "" + Integer.toString(xmlBody.getBytes().length));
                    wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes (xmlBody);

                    // add history event
                    History.getInstance().createNotifyEvent(uriStr, new Date(), null, xmlBody, 0);
                }
            }

            // read response
            int code = conn.getResponseCode();
            return code;
        } finally {
            if (wr != null) {
                wr.flush ();
                wr.close ();
            }

            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Throwable t) {
                    log.error(t.getMessage(), t);
                }
            }
        }

    }

    @Override
    public boolean isLive() {
        return isLive;
    }

    @Override
    public void setLastRead(long timems) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addBytesRead(int len) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() {
        isLive = false;
        log.debug("MediaTunnelProc close id="+id);
        try {
            if (rtspInputStream != null)
                rtspInputStream.close();
            if (rtspOutputStream != null)
                rtspOutputStream.close();
            if (rtspSocket != null)
                rtspSocket.close();
        } catch (Exception ex) {
            log.error("close caught "+ex);
        }
    }

}
