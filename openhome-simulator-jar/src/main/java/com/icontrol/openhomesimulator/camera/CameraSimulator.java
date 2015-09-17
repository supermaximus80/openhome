package com.icontrol.openhomesimulator.camera;

import com.icontrol.data.ConnectInfo;
import com.icontrol.data.PendingPaidKey;
import com.icontrol.data.RegistryEntry;
import com.icontrol.openhome.data.bind.DataBinder;
import com.icontrol.openhomesimulator.camera.resources.CustomEventResource;
import com.icontrol.openhomesimulator.camera.xmppclient.XMPPClient;
import com.icontrol.openhomesimulator.camera.xmppclient.XmppClientIQListener;
import com.icontrol.openhomesimulator.camera.xmppclient.XmppClientSend;
import com.icontrol.openhomesimulator.util.AuthenticationInfo;
import com.icontrol.openhomesimulator.util.OpenHomeProperties;
import com.icontrol.openhomesimulator.util.Utilities;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.HttpRestClient;
import com.icontrol.rest.framework.RestClient;
import com.icontrol.rest.framework.RestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CameraSimulator implements XmppClientSend {

    public static enum XMPP_STATE {IDLE, CONNECTED};

    AuthenticationInfo auth = null;
    SSLSocketFactory factory = null;

    String serialNo;
    String siteID;
    String sharedSecret;
    String credentialGwURL;
    String sessionGw;
    String hostAddress;

    String xmppHost;
    int xmppHostPort;
    String statusStr;
    boolean bootstrapCompleted;
    XMPP_STATE xmppState;
    Logger log;
    XMPPClient xmppClient;
    boolean bEnableXmppMode;
    int notifyAlertID;
    MotionEventAlertNotifier motionEventNotifier;
    XmppClientIQListener xmppClientIQListener;

    public CameraSimulator(String serialNo, Logger log) throws Exception {
        init(serialNo, "", "", "", -1, log);
    }

    public CameraSimulator(String serialNo, String siteID, String sharedSecret, String xmppHost, int xmppPort, Logger log) throws Exception {
        init(serialNo, siteID, sharedSecret, xmppHost, xmppPort, log);
    }

    private void init(String serialNo, String siteID, String sharedSecret, String xmppHost, int xmppPort, Logger log) throws Exception {
        factory = Utilities.getClientSideSSLSocketFactory();
        this.serialNo = serialNo;
        this.siteID = siteID;
        this.sharedSecret = sharedSecret;
        this.credentialGwURL = "";
        this.sessionGw = "";
        this.xmppHost = xmppHost;
        this.xmppHostPort = xmppPort;
        this.statusStr = "NOT STARTED";
        this.bootstrapCompleted = false;
        this.xmppState = XMPP_STATE.IDLE;
        if (log==null)
            log = LoggerFactory.getLogger(CameraSimulator.class);
        this.log = log;
        this.xmppClient = null;
        this.bEnableXmppMode = OpenHomeProperties.getProperty("gatewaySimulator.enableXmpp", false);
        this.notifyAlertID = 100;
        this.motionEventNotifier = null;
        this.xmppClientIQListener = null;

        // if not LWG Camera mode, look for preset camera sharedsecret

        if (!bEnableXmppMode) {
            String sn = OpenHomeProperties.getProperty("cameraSimulator.serialNo");
            String pwd = OpenHomeProperties.getProperty("cameraSimulator.sharedSecret");
            if (serialNo.equalsIgnoreCase(sn) && (sharedSecret==null || sharedSecret.length()==0) && pwd != null) {
                this.sharedSecret = pwd;
            }
        }
    }

    public void destroy() {
        log.debug("CameraSimulator destroy");
        xmppDisconnect();
        xmppClient = null;
        // TODO, other cleanup if necessary
    }

    public String getSiteID() {
        return siteID;
    }

    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;

        // Set serialNo:sharedSecret in authInfo
        if (bEnableXmppMode)
            auth = new AuthenticationInfo(serialNo+"_"+siteID, sharedSecret);
        else
            auth = new AuthenticationInfo(serialNo, sharedSecret);
        log.debug("authInfo: " + auth);
    }

    public String getCredentialGwURL() {
        return credentialGwURL;
    }

    public void setCredentialGwURL(String credentialGwURL) {
        this.credentialGwURL = credentialGwURL;
    }

    public String getSessionGw() {
        return sessionGw;
    }

    public String getXmppGw() {
        return xmppHost + ":" + xmppHostPort;
    }

    public void setXmppGw(String host, int port) {
        xmppHost = host;
        xmppHostPort = port;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public String getStatusStr() {
        return statusStr;
    }

    public boolean finishedBootStrap() {
        return bootstrapCompleted;
    }

    public XMPP_STATE getXMPPstate() {
        return xmppState;
    }

    public Logger getLog() {
        return log;
    }

    /*
        startBootStrap
     */
    public int startBootStrap(String registryGwURL, String serialNo, String activationKey) throws IOException {
        if (registryGwURL==null || serialNo==null || activationKey==null)
            throw new IOException("Invalid argument, contains null") ;
        //
        int retCode = -1;
        try {
            registryGwURL = registryGwURL.trim().toLowerCase();
            if (registryGwURL.endsWith("/"))
                registryGwURL = registryGwURL.substring(0, registryGwURL.length()-1) ;
            serialNo = serialNo.trim().toLowerCase();
            activationKey = activationKey.trim().toLowerCase();

            // step 1 - get Gateway URL and siteID
            getGwURlandSiteID(registryGwURL, serialNo);

            // step 2 - get PendingKey
            getPendingKey(getCredentialGwURL(), getSiteID(), serialNo, activationKey);

            // step 3 - get ConnectInfo
            getConnectInfo(getCredentialGwURL(), getSiteID());

            // connect to xmpp gateway
            if (bEnableXmppMode)
                retCode = xmppConnect();

            // Set serialNo:sharedSecret in authInfo
            if (bEnableXmppMode)
                auth = new AuthenticationInfo(serialNo+"_"+siteID, sharedSecret);
            else
                auth = new AuthenticationInfo(serialNo, sharedSecret);

            // print result
            if (bEnableXmppMode)
                log.debug("Completed bootstrap procedures for serialNo=" + serialNo + " sessionGW= " + getSessionGw() + " xmppGW = " +getXmppGw() + " retCode="+retCode);
            else
                log.debug("Completed bootstrap procedures for serialNo=" + serialNo + " sessionGW= " + getSessionGw() + " retCode="+retCode);
        } catch (FileNotFoundException ex) {
            retCode = 404;
            log.error("startBootStrap caught "+ex,ex);
        } catch (IOException ex) {
            retCode = 500;
            log.error("startBootStrap caught "+ex,ex);
        } catch (Exception ex) {
            retCode = 500;
            log.error("startBootStrap caught "+ex,ex);
        }
        return retCode;
    }

    // Step 1
    public void getGwURlandSiteID(String gwURL, String serialNo) throws Exception {
        gwURL = gwURL + "/" + serialNo;
        log.debug("Start getGwURlandSiteID for serialNo="+serialNo+" URL="+gwURL);
        RegistryEntry registryEntry = (RegistryEntry) request("GET", gwURL, null);
        log.debug("registryEntry: "+registryEntry.toString());
        // set internal var from parsed response
        siteID = registryEntry.getSiteId();
        credentialGwURL = registryEntry.getGatewayUrl();
        statusStr = "Retrieved SiteID" ;
    }

    // Step 2

    public void getPendingKey(String gwURL, String siteID, String serialNo, String activationKey) throws Exception {
        if (gwURL==null || gwURL.length() < 1)
            throw new IOException("Invalid Credential Gateway URL "+gwURL) ;
        bootstrapCompleted = false;
        String reqbody = "serial="+serialNo+"&activationKey="+activationKey;
        if (!gwURL.endsWith("/"))
            gwURL += "/";
        gwURL += "GatewayService/"+siteID+"/PendingDeviceKey" ;
        log.debug("Start getPendingKey for "+reqbody+" credentialGW="+gwURL);
        gwURL=gwURL.toLowerCase();
        PendingPaidKey pk = (PendingPaidKey) request("POST", gwURL, reqbody);
        log.debug("PendingPaidKey return:"+pk.toString());
        // set internal var from parsed response
        sharedSecret = pk.getKey();
        statusStr = "Retrieved Pending Key" ;
    }

    // Step 3
    public void getConnectInfo(String gwURL, String siteID) throws Exception {
        if (gwURL==null || gwURL.length() < 1)
            throw new IOException("Invalid Credential Gateway URL "+gwURL) ;
        if (!gwURL.endsWith("/"))
            gwURL += "/";
        gwURL += "GatewayService/"+siteID+"/connectInfo" ;
        gwURL=gwURL.toLowerCase();
        log.debug("Start getConnectInfo for siteID="+siteID+" credentialGW="+gwURL);
        ConnectInfo info = (ConnectInfo) request("GET", gwURL, null);
        log.debug("ConnectInfo: "+info.toString());
        // set internal var from parsed response
        sessionGw = info.getSession().getHost() +":" + info.getSession().getPort();
        xmppHost = info.getXmpp().getHost() ;
        xmppHostPort = info.getXmpp().getPort().intValue();
        statusStr = "Completed" ;
        bootstrapCompleted = true;
    }


    public Object request(String method, String uriStr, String requestBody) throws Exception{
        int pos=uriStr.indexOf("/rest");
        hostAddress=uriStr.substring(0,pos);

        /*
        if (hostAddress.contains("localhost")){
            hostAddress=hostAddress.replace("localhost","127.0.0.1");
        }
        */
        
        String apiUri=uriStr.substring(pos,uriStr.length());

        RestClient restClient = new HttpRestClient(hostAddress);
        if (method.equals("GET")){
            RestClient.Response response = restClient.GET(apiUri);
                if (response.getStatus() !=200){
                    log.error(method+" to uriStr:"+uriStr+" error. statusCode:"+response.getStatus());
                    throw new IOException(method+" to uriStr:"+uriStr+" error. statusCode:"+response.getStatus());
                }
                else{
                    String standardXML= Utilities.generateStandardXML(response.getContentAsString());
                    if (standardXML.contains("href")){
                        standardXML=Utilities.removeUnusedElementXML("<href>","</href>",standardXML);
                    }
                    return new DataBinder().valueOfXML(new ByteArrayInputStream(standardXML.getBytes()));
                }
            }
        if (method.equals("POST")){

            RestClient.Response response = restClient.POST(apiUri,requestBody);
            if (response.getStatus() !=200){
                log.error(method+" to uriStr:"+uriStr+" error. statusCode:"+response.getStatus());
                throw new IOException(method+" to uriStr:"+uriStr+" error. statusCode:"+response.getStatus());
            }
            else{
                String standardXML= Utilities.generateStandardXML(response.getContentAsString());
                return new DataBinder().valueOfXML(new ByteArrayInputStream(standardXML.getBytes()));
            }
        }
        else throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");


    }



        /* set content type based on whether if body starts with '<' for xml body type */
    private String getContentType(String body) {
        if (body.startsWith("<"))
            return "application/xml";
        else if (body.contains("&"))
            return "application/x-www-form-urlencoded";
        else
            return "text/plain";
    }
    /*
        check xmpp connection state
     */
    public boolean isXMPPconnected() {
        return (xmppClient != null && xmppClient.isConnected()) ;
    }

    /*
        initiate xmpp connection to server
     */
    public int xmppConnect() throws Exception {
        if (isXMPPconnected()) {
            log.debug("XMPP client already connected to host:"+xmppHost+" port:"+xmppHostPort+" serialNo:"+serialNo);
            return 200;
        }
        log.debug("XMPP connecting to host:"+xmppHost+" port:"+xmppHostPort+" serialNo:"+serialNo);
        int code = 500;
        try {
            boolean isConnected = isXMPPconnected();
            if (isConnected)
                return 200;
            if (xmppClient == null) {
                if (xmppHost.equalsIgnoreCase("localhost"))
                    xmppHost = "127.0.0.1";
                xmppClient = new XMPPClient(xmppHost, xmppHostPort);
            }

            xmppClient.setClientAddress(serialNo, siteID);
            xmppClient.setPassword(sharedSecret);                      // sharedSecret
            xmppClient.setSASLAuthenticationEnabled(false);

            // start xmpp connection to server
            code = xmppClient.createXMPPConnection();
        } catch (Exception ex) {
            log.error("XmppConnect failed. Caught "+ex,ex);
            log.error("XMPP NOT connected. serialNo:"+serialNo);
            if (xmppClient != null)
                xmppClient.disconnect();
            xmppClient = null;
        }

        if (code == 200) {
            log.debug("XMPP connection created successfully. serialNo:"+serialNo);
            // add listner
            if (xmppClientIQListener != null)
                xmppClient.setXmppClientIQListener(xmppClientIQListener);

            return code;
        } else  {
            log.error("XMPP connection FAILED!: code: " + code+" serialNo:"+serialNo);
            return code;
        }
    }

    public void xmppDisconnect()  {
        if (!isXMPPconnected()) {
            xmppClient = null;
            return;
        }

        if (xmppClient != null) {
            xmppClient.disconnect();
            xmppClient = null;
        }
    }

    public String getSerialNo() {
        return this.serialNo;
    }

    public XMPPClient getXmppClient() {
        return xmppClient;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return auth;
    }

    public void sendXmppNotification(String method, String action, String body) throws IOException {
        xmppClient.sendNotification(method, action, body);
    }

    public void sendXmppNotification(String method, String action, String body, String packetId) throws IOException {
        xmppClient.sendNotification(method, action, body, packetId);
    }

    public void triggerMotion(int duration) throws Exception {
        int intervalBetweenEvents = CustomEventResource.getIntervalBetweenEvents();

        // check if time past notification interval
        if ( motionEventNotifier != null && motionEventNotifier.hasActiveEvent()) {
            log.error("Ignore motion event due since time since last notfication is < intervalBetweenEvents");
            return;
        }

        String notifyMethod = CustomEventResource.getNotificationMethod();
        log.debug("triggerMotion duration:"+duration+" intervalBetweenEvents:"+intervalBetweenEvents+" notifyMethod:"+notifyMethod);

        if ("HTTP".equalsIgnoreCase(notifyMethod)) {
            log.debug("triggerMotion notify HTTP URL:"+CustomEventResource.getNotifyMethodsHostURL());
            //xxx URL url = new URL(CustomEventResource.getNotifyMethodsHttpHostURL());
        } else if ("XMPP".equalsIgnoreCase(notifyMethod)) {

        }

        // start notification
        AuthenticationInfo authenticationInfo = null;

        String serialNo = OpenHomeProperties.getProperty("cameraSimulator.serialNo");
        boolean requireBasic = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.basic", false);
        boolean requireDigest = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.digest", false);

        if (serialNo != null && (requireBasic || requireDigest)) {
            log.debug("Getting auth info for preset serialNo: " + serialNo);

            CameraSimulator cameraSimulator = CameraSimulatorFactory.getInstance().getCameraInstance(serialNo);
            authenticationInfo = cameraSimulator.getAuthenticationInfo();
        }

        try {
            SSLSocketFactory factory = Utilities.getClientSideSSLSocketFactory();

            // start upload task in a separate thread
            String alertIDstr = Integer.toString(notifyAlertID++);
            motionEventNotifier = new MotionEventAlertNotifier(this, authenticationInfo, factory, log, intervalBetweenEvents, duration);
            motionEventNotifier.start(alertIDstr, CustomEventResource.getNotifyMethodsHostURL());
        } catch (Exception e) {
            log.error("triggerMotion caught: " + e.getMessage());
        }

    }

    public void setXmppClientIQListener(XmppClientIQListener xmppClientIQListener) {
        this.xmppClientIQListener = xmppClientIQListener;
    }

    /**
    public static String convertStreamToString( InputStream is, String ecoding ) throws IOException
    {
        StringBuilder sb = new StringBuilder( Math.max( 16, is.available() ) );
        char[] tmp = new char[ 4096 ];

        try {
            InputStreamReader reader = new InputStreamReader( is, ecoding );
            for( int cnt; ( cnt = reader.read( tmp ) ) > 0; )
                sb.append( tmp, 0, cnt );
        } finally {
            is.close();
        }
        return sb.toString();
    }   */

}
