package com.icontrol.openhomesimulator.camera;

import com.icontrol.openhomesimulator.camera.xmppclient.XmppClientSend;
import com.icontrol.openhomesimulator.util.AuthenticationInfo;
import com.icontrol.openhomesimulator.util.OpenHomeProperties;
import com.icontrol.openhomesimulator.util.URLConnectionHelper;
import com.icontrol.openhomesimulator.util.Utilities;
import org.slf4j.Logger;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.*;

public class MotionEventAlertNotifier   {

    XmppClientSend xmppClientSend;
    AuthenticationInfo auth;
    SSLSocketFactory factory;
    Logger log;
    long intervalBetweenEventsMs;
    long durationMs;
    String id;
    String notifyUrlStr;
    long endTime;
    long index;
    boolean bSentEnd;

    public MotionEventAlertNotifier(XmppClientSend xmppClientSend, AuthenticationInfo auth, SSLSocketFactory factory, Logger log, int intervalBetweenEvents, int duration) {
        this.xmppClientSend = xmppClientSend;
        this.auth = auth;
        this.factory = factory;
        this.log = log;
        this.intervalBetweenEventsMs = intervalBetweenEvents * 1000;
        this.durationMs = duration * 1000;

        this.id = null;
        this.notifyUrlStr = null;
        this.endTime = System.currentTimeMillis() + durationMs;
        this.index = 0;
        this.bSentEnd = true;
    }

    public void start(String id, String notifyUrl) throws IOException {
        this.id = id;
        this.notifyUrlStr = notifyUrl;
        this.endTime = System.currentTimeMillis() + durationMs;
        this.bSentEnd = false;

        if (notifyUrl == null)
            throw new IOException("Null notifyUrl");

        // start upload task in a separate thread
        Timer execTimer = new Timer();
        execTimer.schedule(new InnerRun(), 10);
    }

    public boolean hasActiveEvent() {
        return (System.currentTimeMillis() < endTime);
    }

    private long getNextTimeInterval() {
        long tilEnd = endTime - System.currentTimeMillis();
        if (tilEnd < intervalBetweenEventsMs )
            return tilEnd;
        else
            return intervalBetweenEventsMs;
    }

    private class InnerRun extends TimerTask {
        /*
            TimerTask run
         */
        public synchronized void run() {
            boolean active = true;
            if (System.currentTimeMillis() >= endTime) {
                if (bSentEnd)
                    return;
                else {
                    active = false; // send
                    bSentEnd = true;
                }
            }

            log.debug("PostEventAlert.run id="+id+" index="+index+" activeFlag="+active+" notifyUrlStr="+ notifyUrlStr);
            index++;
            int code = 0;
            try {

                String bodyText = EventAlertMessage.createMessagePIR(id+index, active);
                //log.debug("Sending EventAlert: "+bodyText);
                if (notifyUrlStr.contains("xmpp://"))
                    code = sendViaXMPP("POST", notifyUrlStr, bodyText);
                else
                    code = uploadViaHttp("POST", notifyUrlStr, bodyText) ;
            } catch (Exception ex) {
                log.error("PostEventAlert failed. caught "+ex, ex);
                code = -1;
            }

            if (code != 200 && notifyUrlStr != null) {
                log.error("Notify unsuccessful code="+code+" Post to failure url = "+notifyUrlStr);
            }

            // schedule next timer
            long nextTimer = getNextTimeInterval();
            if (nextTimer > 0) {
                Timer execTimer = new Timer();
                execTimer.schedule(new InnerRun(), nextTimer);
                log.debug("scheduled next Motion Event Trigger at "+nextTimer+" ms later");
            } else {
                log.debug("Finished motion notification sequence");
            }
        }
    }

    public int sendViaXMPP(String method, String urlStr, String body) throws IOException {
        if (method==null || urlStr==null || body==null)
            throw new IOException("sendViaXMPP invalid paramter") ;

        xmppClientSend.sendXmppNotification(method, urlStr, body);
        return 200;
    }

    public int uploadViaHttp(String method, String urlStr, String body) throws IOException {
        if (method==null || urlStr==null || body==null)
            throw new IOException("uploadViaHttp invalid paramter") ;

        HttpURLConnection conn = null;
        OutputStream os = null;
        String contentType = "application/xml";

        log.debug("NotifyEventAlert to url: " + urlStr);

        try {
            HashMap query = new HashMap();
            conn = new URLConnectionHelper(auth, factory).getConnection(urlStr, query);

            // set method
            conn.setRequestMethod(method);
            conn.setDoInput(true);

            String serialNo = OpenHomeProperties.getProperty("cameraSimulator.serialNo");
            boolean requireBasic = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.basic", false);
            boolean requireDigest = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.digest", false);
            if (serialNo != null && (requireBasic || requireDigest) && auth != null && auth.getChallengeResponse() != null) {
                log.debug("challenge response: " + auth.getChallengeResponse());
                conn.setRequestProperty("Authorization", auth.getChallengeResponse());
            }

            // set request headers
            if (method.equals("PUT") || method.equals("POST")) {
                conn.setRequestProperty("Content-Language", "en-US");

                conn.setUseCaches(false);
                conn.setDoOutput(true);

                // set headers
                conn.setRequestProperty("Content-Type", contentType);
                conn.setRequestProperty("Content-Length", "" + Integer.toString(body.length()));

                os = conn.getOutputStream();
                os.write(body.getBytes());
                os.flush();
                os.close();
            }

            Map headerFields = conn.getHeaderFields();
            Iterator iterator = headerFields.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry)iterator.next();
                log.debug("Received Header: " + entry.getKey() + ": " + entry.getValue());
            }

            // read response
            int code = conn.getResponseCode();
            log.debug("code: " + code);

            if (serialNo != null && auth != null && (requireBasic || requireDigest) && code == 401) {
                String authResponse = null;
                if (requireBasic) {
                    authResponse = Utilities.getBasicAuthHeader(auth.getUsername(), auth.getPassword());
                    auth.setChallengeResponse(authResponse);
                }

                if (requireDigest) {
                    try {
                        authResponse = Utilities.getDigestAuthHeader(auth.getUsername(), auth.getPassword(),
                                conn.getRequestMethod(), conn.getURL().getPath(), conn.getHeaderField("WWW-Authenticate"));
                        auth.setChallengeResponse(authResponse);
                    } catch (Exception e) {
                        throw new IOException("Failed to generate challenge response: " + e.getMessage());
                    }
                }

                log.debug("Handling automatic authentication challenge response for serialNo: " + serialNo + " for " + urlStr);
                conn = new URLConnectionHelper(auth, factory).getConnection(urlStr, query);

                // set method
                conn.setRequestMethod(method);
                conn.setDoInput(true);
                conn.setRequestProperty("Authorization", authResponse);

                // set request headers
                if (method.equals("PUT") || method.equals("POST")) {
                    conn.setRequestProperty("Content-Language", "en-US");

                    conn.setUseCaches(false);
                    conn.setDoOutput(true);

                    // set headers
                    conn.setRequestProperty("Content-Type", contentType);
                    conn.setRequestProperty("Content-Length", "" + Integer.toString(body.length()));
                    os = conn.getOutputStream();
                    os.write(body.getBytes());
                    os.flush();
                    os.close();
                }

                // read response
                code = conn.getResponseCode();
                log.debug("Code again: " + code);
            }

            log.debug("Uploaded to url: " + urlStr + " - code: " + code) ;
            return code;
        } finally {
            if (os != null) {
                os.flush ();
                os.close ();
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
}
