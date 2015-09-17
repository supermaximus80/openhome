package com.icontrol.android.openhomesimulator.camera;

import com.icontrol.android.openhomesimulator.util.AuthenticationInfo;
import com.icontrol.android.openhomesimulator.util.OpenHomeProperties;
import com.icontrol.android.openhomesimulator.util.Utilities;
import com.icontrol.openhome.data.ConnectInfo;
import com.icontrol.openhome.data.PendingPaidKey;
import com.icontrol.openhome.data.RegistryEntry;
import com.icontrol.openhome.data.bind.DataBinder;
import com.icontrol.rest.framework.HttpRestClient;
import com.icontrol.rest.framework.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CameraSimulator{

    public static enum XMPP_STATE {IDLE, CONNECTED};

    AuthenticationInfo auth = null;
    SSLSocketFactory factory = null;

    String serialNo;
    String siteID;
    String sharedSecret;
    String credentialGwURL;
    String sessionGw;

    String xmppHost;
    int xmppHostPort;
    String statusStr;
    boolean bootstrapCompleted;
    XMPP_STATE xmppState;
    Logger log;
    //XMPPClient xmppClient;
    boolean bEnableXmppMode;
    int notifyAlertID;
    //MotionEventAlertNotifier motionEventNotifier;
    //XmppClientIQListener xmppClientIQListener;

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
        //this.xmppClient = null;
        this.bEnableXmppMode = OpenHomeProperties.getProperty("gatewaySimulator.enableXmpp", false);
        this.notifyAlertID = 100;
        //this.motionEventNotifier = null;
        //this.xmppClientIQListener = null;

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
        //xmppDisconnect();
        //xmppClient = null;
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
    public AuthenticationInfo getAuthenticationInfo() {
        return auth;
    }


    /*
        startBootStrap
     */
    public int startBootStrap(String registryGwURL, String serialNo, String activationKey) throws IOException  {
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
                //retCode = xmppConnect();

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
        System.out.println("registryEntry: "+registryEntry.toString());

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
        System.out.println("Start getPendingKey for "+reqbody+" credentialGW="+gwURL);
        gwURL=gwURL.toLowerCase();
        PendingPaidKey pk = (PendingPaidKey) request("POST", gwURL, reqbody);
        System.out.println("PendingPaidKey return:"+pk.toString());

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
        System.out.println("ConnectInfo: "+info.toString());

        // set internal var from parsed response
        sessionGw = info.getSession().getHost() +":" + info.getSession().getPort();
        xmppHost = info.getXmpp().getHost() ;
        xmppHostPort = info.getXmpp().getPort().intValue();

        statusStr = "Completed" ;
        bootstrapCompleted = true;
    }


    public Object request(String method, String uriStr, String requestBody) throws Exception{
        String host="http://10.0.2.2:18080";
        String localhost="http://127.0.0.1:18080";
        if (uriStr.contains(host)){
            uriStr = uriStr.replace(host,"");
        }
        if (uriStr.contains(localhost)){
            uriStr=uriStr.replace(localhost,"");
        }
        String remoteHost=CameraSimulatorFactory.getInstance().getOpenHomeApiPath();
        if (remoteHost.contains(localhost)){
            remoteHost=remoteHost.replace(localhost,host);
        }
        RestClient restClient = new HttpRestClient(remoteHost); // stub here TODO: if need to pass server name to a class variable.
        if (method.equals("GET")){
            RestClient.Response response = restClient.GET(uriStr);
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

            RestClient.Response response = restClient.POST(uriStr, requestBody);
            if (response.getStatus() !=200){
                log.error(method + " to uriStr:" + uriStr + " error. statusCode:" + response.getStatus());
                throw new IOException(method+" to uriStr:"+uriStr+" error. statusCode:"+response.getStatus());
            }
            else{
                String standardXML= Utilities.generateStandardXML(response.getContentAsString());
                return new DataBinder().valueOfXML(new ByteArrayInputStream(standardXML.getBytes()));
            }
        }
        else throw new IOException(method+ " to uriStr: " +" doesn't exist"); //temporary stub. TODO: add conditions to handle other methods like PUT and DELETE


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
}
