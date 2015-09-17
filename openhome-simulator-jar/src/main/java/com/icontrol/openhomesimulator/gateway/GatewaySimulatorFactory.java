package com.icontrol.openhomesimulator.gateway;

import com.icontrol.ohcm.OpenHomeCameraDriver;
import com.icontrol.ohsimsolver.ResponseStatusFactory;
import com.icontrol.openhome.data.ResponseStatus;
import com.icontrol.openhomesimulator.camera.CameraSimulatorFactory;
import com.icontrol.openhomesimulator.camera.resources.CustomEventResource;
import com.icontrol.openhomesimulator.gateway.simplerelay.SimpleRelayManager;
import com.icontrol.openhomesimulator.gateway.xmppserver.*;
import com.icontrol.openhomesimulator.util.AuthenticationInfo;
import com.icontrol.openhomesimulator.util.OpenHomeProperties;
import com.icontrol.openhomesimulator.util.Utilities;
import com.icontrol.rest.framework.LocalRestClient;
import com.icontrol.rest.framework.RestClient;
import com.icontrol.rest.framework.RestConstants;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.XMPPServerInfo;
import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmpp.packet.JID;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class GatewaySimulatorFactory {

    /*
        static public variables
     */
    static public String TEST_ID = "0";
    static public int RTSP_ACTION_CODE = -500;
    static public String ALERT_CONNECTION = "alertconnect" ;
    static public String ALERT_DISCONNECT = "alertdisconnect" ;
    /*
        static variables and methods
     */
    static private String pathPrefix = "/OpenHome/";

    static private String manualPathName = "manualEntry";

    static private String manualCameraURL = "enterCameraURL";

    private final static Object monitor = new Object();

    static private GatewaySimulatorFactory simulator;

    protected static final Logger log = LoggerFactory.getLogger(GatewaySimulatorFactory.class);

    static public GatewaySimulatorFactory getInstance()  {
            synchronized(monitor) {
                if (simulator == null) {
                    try {
                        simulator = new GatewaySimulatorFactory();
                        simulator.start();
                    } catch (Exception e) {
                        log.error("Unable to create GatewaySimulator. Caught "+e, e);
                        return null;
                    }
                }
                return simulator;
            }
        }

    public void initCustomEventResource(){
        CustomEventResource.init();
    }

    static public GatewaySimulatorFactory createInstance() throws Exception {
        synchronized(monitor) {
            if (simulator == null) {
                simulator = new GatewaySimulatorFactory();
                simulator.start();
            }
        }
        return simulator;
    }

    static public void destroy() {
        synchronized(monitor) {
            if (simulator != null) {
                simulator.stop();
            }
        }
    }

    /*
        class variables and methods
     */
    private int MAX_MEDIA_STORAGE_ENTRIES = 100;
    String contextPath;
    ConcurrentHashMap<String, GatewaySimulator> simulatorMap ;
    TreeSet<String> apiPaths = new TreeSet<String>();
    //Map<String, ResourceIf> resourceMap;
    Map<String, MediaInstance> mediaStoreMap;
    Queue<MediaInstance> mediaStoreNotifyQueue;

    Map<String, AuthenticationInfo> authInfoMap;

    // XMPP server
    XMPPServer xmppServer;
    String xmppServerDomainName;
    int xmppServerPort = 5222;
    KeepAliveTimerManager keepAliveTimerManager;
    boolean bXmppHostConfigured;

    // UI improvements
    String lastCameraURL;
    String lastUsername;
    String lastPassword;

    private GatewayRestService restServiceInst = null;
    private String uniqueId = Long.toString(System.currentTimeMillis()).substring(5);
    private SimpleRelayManager simpleRelayManager = null;
    private boolean bDisableGatewayMode = false;
    private boolean bEnableXmppMode = false;


    protected GatewaySimulatorFactory()  {
        contextPath = "";
        simulatorMap = new ConcurrentHashMap<String, GatewaySimulator>();
        apiPaths = new TreeSet<String>();
        //resourceMap = new HashMap<String, ResourceIf>();
        mediaStoreMap = new ConcurrentHashMap<String, MediaInstance> ();
        mediaStoreNotifyQueue = new ConcurrentLinkedQueue<MediaInstance>();
        xmppServer = null;
        xmppServerDomainName = null;
        keepAliveTimerManager = null;
        bXmppHostConfigured = false;

        lastCameraURL = null;
        lastUsername = null;
        lastPassword = null;

        bDisableGatewayMode = OpenHomeProperties.getProperty("gatewaySimulator.disabled", false);
        bEnableXmppMode = OpenHomeProperties.getProperty("gatewaySimulator.enableXmpp", false);
        if (!bDisableGatewayMode)
            restServiceInst = new GatewayRestService();
        System.out.println("created new restImplInst");
        simpleRelayManager = null;

        // init CameraRestServiceHandler in order to create a map of OpenHome API resources
        apiPaths.addAll(OpenHomeCameraDriver.defaultCommandMap.keySet());

        authInfoMap = new HashMap<String, AuthenticationInfo>();
    }

    public void start() throws Exception {
        if (bDisableGatewayMode)
        {return;}

        log.debug("GatewaySimulatorFactory.start");
        // initialize relay manager
        simpleRelayManager = SimpleRelayManager.createInstance();

        // initialize XMPP server
        if (!bEnableXmppMode)
            return;
        String baseDir = System.getProperty(OpenHomeProperties.BASE_PATH, "/data/ic");
        String propFileName = baseDir.endsWith("/")?baseDir:(baseDir+"/") + "conf/org.jivesoftware.xmpp.xml";
        // test if propFile exists to override local resource
        File file = new File(propFileName);
        if (file.exists() && file.canRead()) {
            JiveGlobals.setGlobalPropertiesFile(propFileName);
        }

        // set custome auth and user providers, must be called after JiveGlobals props are loaded
        JiveGlobals.setProperty("provider.auth.className", "com.icontrol.openhomesimulator.gateway.xmppserver.AuthProviderXmpp");
        JiveGlobals.setProperty("provider.user.className", "com.icontrol.openhomesimulator.gateway.xmppserver.UserProviderXmpp");

        xmppServer = createXMPPServer();
        keepAliveTimerManager = new KeepAliveTimerManager();
        log.debug("GatewaySimulatorFactory.start successfully.");
    }

    public void stop()  {
        log.debug("GatewaySimulatorFactory.stop");
        SimpleRelayManager.end();
        if (xmppServer != null) {
            log.debug("GatewaySimulatorFactory XMPP server stop.");
            xmppServer.stop();
            xmppServer = null;
        }
        if (keepAliveTimerManager != null)
            keepAliveTimerManager.close();
    }

    /*

     */
    /*
    public Map<String, ResourceIf> getResourceMap() {

        return restServiceInst.getResourceMap() ;
    }
    */

    public Map<String, AuthenticationInfo> getAuthInfoMap() {
        return authInfoMap;
    }

    public GatewayRestService getRestServiceInst() {
        return restServiceInst;
    }

    public String getUniqueID() {
        return uniqueId;
    }

    protected String getPathFromResourceDescription(String str) {
        int pos = str.indexOf(" ");
        if (pos==-1)
            return null;
        return str.substring(pos + 1);
    }

    public Iterator<String> getAPIpaths() {
        return apiPaths.iterator();
    }

    public String getPAthPrefix() {
        return pathPrefix;
    }

    public String getManualPathName() {
        return manualPathName;
    }

    public String getManualCameraURL() {
        return manualCameraURL;
    }

    public String getLastCameraURL() {
        return lastCameraURL;
    }

    public String getLastUsername() {
        return lastUsername;
    }

    public String getLastPassword() {
        return lastPassword;
    }

    public boolean supportMethodForPath(String method, String path) {
        path = path.replace("/OpenHome","[partner]").replace("0","[NOTIFYID]");
        path = path.replace("0","[REGIONID]");

        //sort out the resource endpoints which don't have a GET method
        //(GET is a default for resource endpoints)
        List<String> list = Arrays.asList(
                "[partner]/System/factoryReset",
                "[partner]/System/reboot",
                "[partner]/System/updateFirmware",
                "[partner]/Streaming/channels/[UID]/picture/upload",
                "[partner]/Streaming/channels/[UID]/video/upload",
                "[partner]/Streaming/MediaTunnel/[UID]/destroy",
                "[partner]/Streaming/MediaTunnel/create"
                //"[partner]/api/xsd"
        );
        if (method.equalsIgnoreCase("GET") && (!list.contains(path))){
            return true;
        }
        else{
            try{
                Document api= CameraSimulatorFactory.getInstance().getCameraApi();
                XPath xpath = CameraSimulatorFactory.getInstance().getXpath();
                return (Boolean)xpath.evaluate("//resource[@path='"+path+"']/functions/function[@method='"+method+"']", api, XPathConstants.BOOLEAN);
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
    }

    public String getExampleRequestXML(String selectedURI) throws Exception {
        LocalRestClient localRestClient = CameraSimulatorFactory.getInstance().getLocalRestClient();
        RestClient.Response response = localRestClient.GET(selectedURI+"/example");
        if (response.getStatus()==200){
            String content = response.getContentAsString();
            log.error("WBB content: " + content);
            if (content.startsWith("<?xml"))
                return toPrettyXML(content);
            else
                return content;
        }
        else return "";
    }

    public void setContextPath(HttpServletRequest request) throws Exception {
        String protocol = request.getRequestURL().toString().toLowerCase();
        int pos = protocol.indexOf(":") ;
        if (pos > 0)
            protocol = protocol.substring(0, pos) ;
        else
            protocol = "https";

        contextPath = protocol +"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();

        // set context for relay server as well
        if (simpleRelayManager != null)
            simpleRelayManager.setContextPath(request);
    }

    /*
        Returns true if the uri can lead to media upload
     */
    public boolean isUploadMedia(String uri) {
        // TODO, add additional upload uri when needed
        if (uri.endsWith("vide/upload"))
            return true;
        else if (uri.endsWith("picture/upload"))
            return true;
            //else if (mediaStoreNotifyQueue.size() > 0)
            //    return true;
        else
            return false;
    }

    /*
        processRequest
     */
    public int processRequest(String method, String uri, String cameraURL, StringBuilder responseSB, String requestStr, StringBuilder rtspUrlSB, String username, String password) throws Exception  {
        uri = uri.trim();
        // retrieve existing CameraSimEngine, if not, create new one
        GatewaySimulator engine = simulatorMap.get(cameraURL);
        if (engine == null) {
            engine = new GatewaySimulator(cameraURL, xmppServer);
            simulatorMap.put(cameraURL, engine);
        }

        // save last use data
        lastCameraURL = cameraURL;
        lastUsername = username;
        lastPassword = password;

        //
        int retCode = -1;
        try {
            // if command to create tunnel, create relay first
            String relaySessionID = null;
            if (isCreateTunnelCommand(uri)) {
                relaySessionID = SimpleRelayManager.getInstance().createRelay();
            }

            AuthenticationInfo authInfo = getAuthInfoMap().get(username);
            if (authInfo == null) {
                authInfo = new AuthenticationInfo(username, password);
                getAuthInfoMap().put(username, authInfo);
            }

            // process
            retCode = engine.process(method, uri, cameraURL, responseSB, requestStr, authInfo);

            // if media Tunnel create command, display video
            if (retCode == 200 && relaySessionID != null && responseStatusOK(responseSB)) {
                retCode = RTSP_ACTION_CODE;
                rtspUrlSB.append(SimpleRelayManager.getInstance().getUserRelayURL(relaySessionID));
            } else if (retCode != 200 &&relaySessionID != null) { // command failed, remove relay
                SimpleRelayManager.getInstance().removeRelay(relaySessionID);
            }
        } catch (FileNotFoundException ex) {
            retCode = 404;
            responseSB.append(ex) ;
        } catch (IOException ex) {
            responseSB.append(ex) ;
        }

        return retCode;
    }

    /*
        responseSB contains ResponseStatus XML
     */
    public boolean responseStatusOK(StringBuilder responseSB) {
        try {
            ResponseStatus status = (ResponseStatus) RestClient.getObject(responseSB.toString(), RestConstants.ContentType.TEXT_XML,ResponseStatus.class);
            if (status.getStatusCode() == ResponseStatusFactory.STATUSCODE.OK.ordinal() || status.getStatusCode() == ResponseStatusFactory.STATUSCODE.NO_ERROR.ordinal())
                return true;
            else
                return false;
        } catch (Exception e) {
            log.error("GatewaySimulatorFactor unmarshall error: "+e);
            return false;
        }
    }

    /*
        disconnectXMPP
     */
    public void disconnectXMPP(String cameraURL) throws Exception  {
        if (cameraURL==null)
            return;
        // retrieve existing CameraSimEngine, if not, create new one
        GatewaySimulator engine = simulatorMap.get(cameraURL);
        if (engine == null) {
            engine = new GatewaySimulator(cameraURL, xmppServer);
            simulatorMap.put(cameraURL, engine);
        }

        engine.disconnectXMPP();
    }

    /*
        isXmppCnnected
     */
    public boolean isXmppCnnected(String cameraURL) throws Exception {
        if (cameraURL==null)
            return false;
        // retrieve existing CameraSimEngine, if not, create new one
        GatewaySimulator engine = simulatorMap.get(cameraURL);
        if (engine == null) {
            engine = new GatewaySimulator(cameraURL, xmppServer);
            simulatorMap.put(cameraURL, engine);
        }

        return  engine.isXmppCnnected(cameraURL);
    }

    public boolean isCreateTunnelCommand(String uri) {
        if (uri != null && uri.endsWith("streaming/mediatunnel/create"))
            return true;
        else
            return false;
    }

    /*
        getMediaUploadURLPrefix TODO: determine path without manual /gw/
     */
    public String getMediaUploadURLPrefix() {
        return contextPath+"/gw/rest/";
    }

    /*
        getRelayPrefix TODO: determine path without manual /gw/
     */
    public String getRelayPrefix() {
        return contextPath+"/gw/";
    }

    /*

    /*
        storeMedia
     */
    public void storeMedia(String id, byte[] media, String contentType) {
        if (mediaStoreMap.size() > MAX_MEDIA_STORAGE_ENTRIES)
            mediaStoreMap.clear();
        if (mediaStoreNotifyQueue.size() > MAX_MEDIA_STORAGE_ENTRIES)
            mediaStoreNotifyQueue.clear();
        MediaInstance m = new MediaInstance(new Date(), media, contentType, id);
        mediaStoreMap.put(id, m);
        mediaStoreNotifyQueue.add(m) ;
    }

    /*
        getMedia
     */
    public MediaInstance getMedia(String id) {
        return mediaStoreMap.get(id);
    }

    /*
        storeNotification
     */
    public void storeNotification(String id, String notifyStr, String contentType) {
        if (mediaStoreNotifyQueue.size() > MAX_MEDIA_STORAGE_ENTRIES)
            mediaStoreNotifyQueue.clear();
        MediaInstance m = new MediaInstance(new Date(), notifyStr.getBytes(), contentType, id);
        mediaStoreNotifyQueue.add(m) ;
    }

    /*
        getUserNotification
     */
    public MediaInstance getUserNotification() {
        return mediaStoreNotifyQueue.poll();
    }


    //

    /*
        getExampleXML()

        examine annotation to look for methods that support HasExample
    */
    /*
    private String getExampleXML(ResourceIf r) throws Exception {
        Class cls = r.getPerformMethod().getDeclaringClass();
        Method methlist[] = cls.getDeclaredMethods();
        for (int i=0; i<methlist.length; i++) {
            Method m = methlist[i];
            HasExample example = m.getAnnotation(HasExample.class);
            if (example != null) {
                Object[] params = null;
                Object responseObject = m.invoke(m, params);
                if (responseObject instanceof String)
                    return Utilities.prettyFormat((String) responseObject);
                else{

                    try {
                        String s = new DataBinder().toXML(responseObject);
                        final Document document = parseXmlFile(Utilities.getCleanXml(s));//Utilities.getCleanXml(LocalDataBinder.toString(responseObject)));

                        OutputFormat format = new OutputFormat(document);
                        format.setLineWidth(65);
                        format.setIndenting(true);
                        format.setIndent(2);
                        Writer out = new StringWriter();
                        XMLSerializer serializer = new XMLSerializer(out, format);
                        serializer.serialize(document);

                        return out.toString();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return "";
    }
    */

    private String toPrettyXML(String xml){

        try {
            log.error("Wbb pretty xml: " + xml);
            final Document document = parseXmlFile(Utilities.getCleanXml(xml));//Utilities.getCleanXml(LocalDataBinder.toString(responseObject)));
            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /*
        getCameraUserName
     */
    public String getCameraUserName(String cameraURL) {
        return "administrator"; // TODO, use username retrieved from camera
    }

    /*
        getCameraAdminUserName
     */
    public String getCameraUserPassword(String cameraURL) {
        return ""; // TODO, use username retrieved from camera
    }

    /*
        getCheckEvent, id needed to check for new events
     */
    public String getCheckEvent() {
        return getUniqueID();
    }

    /*
        XMPP Server
     */
    public XMPPServer getXMPPServer() throws IOException {
        if (xmppServer == null)
            throw new IOException("XMPP Server not initialized yet") ;
        else
            return xmppServer;
    }

    private XMPPServer createXMPPServer() throws Exception {
        log.error("XMPP createXMPPServer");

        // get local listen IP address as XMPP server domain
        xmppServerDomainName = JiveGlobals.getProperty("xmpp.domain", "").trim();
        if (xmppServerDomainName == null || xmppServerDomainName.length()==0) {
            xmppServerDomainName = "127.0.0.1";
            bXmppHostConfigured = false;
            log.debug("createXMPPServer xmpp.domain not configured. user default domain="+xmppServerDomainName);
        } else {
            bXmppHostConfigured = true;
            log.debug("createXMPPServer user configured xmpp.domain="+xmppServerDomainName);
        }

        // set XMPP server domain name
        JiveGlobals.setProperty("xmpp.domain", xmppServerDomainName);

        XMPPServer server = new XMPPServer();
        ClientSessionTimeoutHandler timeoutHandler = new ClientSessionTimeoutHandler();
        ClientSessionCloseCallbackHandler closeHandler = new ClientSessionCloseCallbackHandler();
        SessionEventListener sessionEventListener = new SessionEventListenerHandler();
        ServerIQHandler iqHandler = new ServerIQHandler();

        server.setClientSessionCloseCallback(closeHandler);
        server.setClientSessionTimeoutConfig(timeoutHandler);
        server.setSessionEventListener(sessionEventListener);
        server.setAdditionalIQHandler(iqHandler);

        try {
            server.start();
        } catch (Exception ex) {
            log.error("XMPP start caught "+ex);
        }
        if (!server.isStarted()) {
            log.error("XMPP start failed.");
            throw new IOException("XMPP start failed") ;
        }

        XMPPServerInfo xmppServerInfo = server.getServerInfo() ;
        if (bXmppHostConfigured && !xmppServerDomainName.equalsIgnoreCase(xmppServerInfo.getName().trim())) {
            log.error("createXMPPServer actual XMPP hostName:"+xmppServerInfo.getName().trim()+" differs from configured name:"+xmppServerDomainName);
            throw new Exception("createXMPPServer actual XMPP hostName:"+xmppServerInfo.getName().trim()+" differs from configured name:"+xmppServerDomainName);
        } else {
            xmppServerDomainName = xmppServerInfo.getName().trim();
        }

        log.debug("XMPP Server started successfully. version=" + xmppServerInfo.getVersion().getVersionString() + " domain="+xmppServerDomainName+" port="+xmppServerPort);

        return server;
    }

    // should only be used by Registry resource to provide ConnectInfo to remote camera
    public String getXmppServerDomainName(HttpServletRequest req) {
        /*
            if xmppHost is configured in xmpp.xml, use specified hostname, otherwise, return ip address in ServerletRequest
         */
        if (bXmppHostConfigured)
            return xmppServerDomainName;
        else
            return req.getServerName();
    }

    // get xmpp server domain name use in XMPP server
    public String getXmppServerDomainName() {
        return xmppServerDomainName;
    }

    public int getXmppServerPort() {
        return xmppServerPort;
    }

    public int numXmppClientSessions() {
        if (xmppServer == null) return 0;

        try {
            SessionManager sessionManager = xmppServer.getSessionManager();
            return sessionManager.getSessions().size();
        } catch (Exception ex) {
            log.error("numXmppClientSessions caught "+ex,ex);
            return 0;
        }
    }

    public Iterator<String> getXmppClientSessions() throws Exception {
        TreeSet<String> xmppClientList = new TreeSet<String>();
        if (xmppServer == null)
            return xmppClientList.iterator();
        SessionManager sessionManager = xmppServer.getSessionManager();
        Collection<ClientSession> sessions = sessionManager.getSessions();
        Iterator<ClientSession> iter = sessions.iterator();
        while(iter.hasNext()) {
            ClientSession s = iter.next();
            JID jid = s.getAddress();
            log.debug("Found in xmppClientSession: node="+jid.getNode()+" domain="+jid.getDomain() + " resource="+jid.getResource()) ;
            xmppClientList.add(jid.toString());
        }
        return xmppClientList.iterator();
    }

    /*
        broadcast to every gateway instance
     */
    public void notifyXmppReceivePacket(ServerIQResponseParser iqResponse) {
        for (Iterator<GatewaySimulator> i = simulatorMap.values().iterator(); i.hasNext();)  {
            i.next().notifyXmppReceivePacket(iqResponse);
        }
    }

    /*
        broadcast to every gateway instance
     */
    public void notifyXmppRequestPacket(ServerRequestIQParser iqRequest) {
        for (Iterator<GatewaySimulator> i = simulatorMap.values().iterator(); i.hasNext();)  {
            i.next().notifyXmppRequestPacket(iqRequest);
        }
    }

    public void setDisableGatewayMode(boolean mode) {
        bDisableGatewayMode = mode;
    }

    public boolean isbEnableXmppMode() {
        return bEnableXmppMode;
    }

    // use by camera class to start/stop keep-alive timer
    public void notifyNewConnection(ClientSession cs) {
        String jidStr = cs.getAddress().toString();
        log.debug("notifyNewConnection jid:"+cs.getAddress());
        try {
            GatewaySimulator engine = simulatorMap.get(jidStr);
            if (engine == null) {
                engine = new GatewaySimulator(jidStr, xmppServer);
                simulatorMap.put(jidStr, engine);
            }

            // start keep alive timer
            if (keepAliveTimerManager != null)
                keepAliveTimerManager.addConnection(cs, ClientSessionTimeoutHandler.getTimeoutDefault());

            // announce new session via alert box
            GatewaySimulatorFactory.getInstance().storeNotification(Long.toString(System.currentTimeMillis()), jidStr, GatewaySimulatorFactory.ALERT_CONNECTION);

        } catch (Exception ex) {
            log.error("notifyNewConnection caught "+ex);
        }
    }

    /*
    private class NewConnectionCheckTask  extends TimerTask {
        String jidStr;

        public NewConnectionCheckTask(String jidStr) {
            this.jidStr = jidStr;
        }

        public void run() {
            try {
                ClientSession clientSession = getXmppClientSession(new JID(jidStr));
                if (clientSession == null) {
                    log.error("NewConnectionCheckTask unable to find client session for JID:"+jidStr);
                } else {
                    // start keep alive timer
                    if (keepAliveTimerManager != null)
                        keepAliveTimerManager.addConnection(clientSession, ClientSessionTimeoutHandler.getTimeoutDefault());

                    // announce new session via alert box
                    GatewaySimulatorFactory.getInstance().storeNotification(Long.toString(System.currentTimeMillis()), jidStr, GatewaySimulatorFactory.ALERT_CONNECTION);
                }
            } catch (Exception ex) {
                log.error("NewConnectionCheckTask.run caught "+ex);
            }
        }
    }
    */

    public void notifyDisconnected(LocalClientSession session) {
        try {
            String jidStr = session.getUsername();
            log.debug("notifyDisconnected jid:"+jidStr);
            //GatewaySimulator engine = simulatorMap.get(jidStr);

            GatewaySimulatorFactory.getInstance().storeNotification(Long.toString(System.currentTimeMillis()), jidStr, GatewaySimulatorFactory.ALERT_DISCONNECT);
        } catch (Exception ex) {
            log.error("notifyDisconnected caught "+ex);
        }
    }

    /*
         sendRequestIQ entry point to send a request message to a client
         returns packetID for the message
     */
    public String sendRequestIQ(JID from, ClientSession cs, String method, String uri, Map<String, String> headerMap, String bodyText) throws Exception {
        if (xmppServer == null)
            throw new IOException("XMPP Server in stoppped state");
        // check if session is still connected
        if (cs == null || cs.getStatus() < ClientSession.STATUS_CONNECTED)
            throw new IOException("Client not connected");

        ServerRequestIQ requestIQ = new ServerRequestIQ(from, cs.getAddress(), method, uri, headerMap, bodyText);

        // Send message
        xmppServer.getPacketDeliverer().deliver(requestIQ);

        return requestIQ.getID();
    }

    public ClientSession getXmppClientSession(JID jid) throws Exception {
        return xmppServer.getSessionManager().getSession(jid);
    }


    private Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new


                    RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /*
    public String gatewaySimulatorTester(String cameraURL){



   Iterator<String> iter = simulator.getAPIpaths();
   StringBuilder testResult=new StringBuilder();
   StringBuilder testResultConclusion=new StringBuilder();
   int unknownError=0; int httpError=0; int skip=0; int success=0; int total=0;
   int getError=0; int putError=0; int postError=0; int deleteError=0;
   testResult.append("Test for Gateway Simulator HTTP requests to Camera Simulator").
           append("\n" + "Gateway at: " + this.contextPath +
                   "\n" + "Camera at " + cameraURL);
   while (iter.hasNext()) {
       total++;
       String path = iter.next().replaceFirst("\\[partner\\]/",simulator.getPAthPrefix());
       String id=path.contains("wireless")?"1":"0";
       String endpointPath=path.replaceFirst(simulator.getPAthPrefix(), "\\[partner\\]/");
       String requestBody;

       //skip the request on auto-generated paths
       if (path.equals("/openhome/api/doc")||path.equals("/openhome/api")||path.equals("/openhome/api/xsd")
               ||path.contains("mediatunnel")){
           testResult.append("\n" +
                   "============================================" +
                   "\n"+"URI: "+path+
                   "\n"+"Skipped"+"\n" +
                   "============================================"
           );
           if (path.contains("mediatunnel")){
               testResultConclusion.append("URI: "+path+" Method: "+"ALL" +" Result: "+"Skipped"
                       +" Reason: Media Tunnel (Relay) features not enabled yet.\n");
           }
           else {
               testResultConclusion.append("URI: "+path+" Method: "+"ALL" +" Result: "+"Skipped"
                       +" Reason: Skip on auto-generated api endpoints to save space.\n");
           }
           skip++;
           continue;
       }

       //get request body from Example endpoint from resources
       try{
           requestBody=getExampleRequestXML(path);
       }
       catch (Exception e){
           requestBody="";
           testResult.append("\n" +
                   "============================================" +
                   "\n"+"URI: "+path+
                   "\n"+"Failed to get example request."+
                   "\n" +
                   "============================================"
           );
           testResultConclusion.append("URI: "+path+" GET EXAMPLE REQUEST"+" Result: "+"Failure"
                   +" Reason: Unknown\n");
           unknownError++;
       }

       /*
           Go through every method, check if it is enabled at the endpoint first and then implement it.
           GET:    check if response xml returned is empty. If empty, failed.
           PUT:    check if response xml is Response OK. If not, failed. Then do GET to check if PUT successfully
                   at camera endpoint.
           POST:   check if response xml is Response OK. If not, failed.
                   And if PUT is also enabled and this endpont, skip POST.
           DELETE: check if response xml is Response OK. If not, failed.

        */
    /*
                String method="";
                try{
                    path=path.contains("[UID]")?path.trim().replaceAll("\\[id\\]",id):path;
                    method="GET";
                    if (simulator.supportMethodForPath(method,endpointPath)){
                        StringBuilder responseSB = new StringBuilder();
                        processRequest(method, path, cameraURL, responseSB, requestBody, null, null, null);
                        testResult.append("\n============================================" +
                                "\n" + "URI: " + path +
                                "\nMethod: " + method
                                + "\n" + "Response: \n\n" +
                                Utilities.prettyFormat(responseSB.toString()) + "\n" +
                                "============================================"
                        );
                        if (responseSB.toString().equals("")||responseSB.toString()==null){
                            getError++;
                            testResultConclusion.append("URI: "+path+" Method: "+method+" Result: "+"Failure\n");
                        }
                        else{
                            success++;
                            testResultConclusion.append("URI: "+path+" Method: "+method+" Result: "+"Success\n");
                        }

                    }

                    method="PUT";
                    if (simulator.supportMethodForPath(method,endpointPath)){
                        StringBuilder responseSB = new StringBuilder();
                        //skip PUT if no exmaple request body given
                        if (requestBody.equals("")||requestBody==null){
                            testResult.append("\n" +
                                    "============================================" +
                                    "\n"+"URI: "+path+
                                    "\nMethod: PUT"
                                    +"\nNo example request provided."
                                    +"\n"+"Skipped"+"\n" +
                                    "============================================"
                            );
                            testResultConclusion.append("URI: "+path+" Method: "+"PUT"+" Result: "+"Skipped"
                                    +" Reason: No example request provided.\n");
                            skip++;
                        }
                        else{

                            processRequest(method,path,cameraURL,responseSB,requestBody,null,null,null);
                            testResult.append("\n============================================" +
                                    "\n"+"URI: "+path+
                                    "\nMethod: "+method
                                    +"\nRequest body: "+
                                    "\n"+Utilities.prettyFormat(requestBody)
                                    +"\n"+"Response: \n\n"+
                                    Utilities.prettyFormat(responseSB.toString())+"\n"+
                                    ""
                            );

                            String response=Utilities.removeSpacesXml(responseSB.toString());
                            StringReader source=new StringReader(response);
                            Document document=Utilities.getDocument(source);
                            Object responseObject= LocalDataBinder.valueOf(document);

                            if (((ResponseStatus)responseObject).getStatusCode()==1||
                                    ((ResponseStatus)responseObject).getStatusCode()==7){
                                success++;
                                testResultConclusion.append("URI: "+path+" Method: "+method+" Result: "+"Success\n");
                                StringBuilder newResponseSB = new StringBuilder();
                                boolean equivalent=false;
                                if (simulator.supportMethodForPath("GET",endpointPath)){
                                    processRequest("GET", path, cameraURL, newResponseSB, null, null, null, null);
                                    testResult.append(
                                            "\n Do GET again to check if " + method + " successfully" +
                                                    "\n" + "Response: " +
                                                    "\n\n" + Utilities.prettyFormat(newResponseSB.toString())
                                    );
                                    if (!requestBody.startsWith("<?")){
                                        equivalent=requestBody.equals(newResponseSB.toString());
                                    }
                                    else {
                                        String newResponse=Utilities.removeSpacesXml(newResponseSB.toString());
                                        StringReader newSource=new StringReader(newResponse);
                                        Document newDocument=Utilities.getDocument(newSource);
                                        Object newResponseObject= LocalDataBinder.valueOf(newDocument);

                                        if (GatewaySimulator.requestXMLtoObject instanceof DeviceInfo){

                                            UUIDCap uuid = new UUIDCap();
                                            uuid.setValue("f81d4fae-7dec-11d0-1111-00a0c91e6bf6");    // just a random fake uuid
                                            ((DeviceInfo)GatewaySimulator.requestXMLtoObject).setDeviceID(uuid);

                                            ((DeviceInfo)GatewaySimulator.requestXMLtoObject).setModel(Wrappers.createStringCap("Model 100"));
                                            ((DeviceInfo)GatewaySimulator.requestXMLtoObject).setSerialNumber(Wrappers.createStringCap("11223344556600"));
                                            MACCap mac = new MACCap();
                                            mac.setValue("00-B0-D0-86-BB-F7");      // random example
                                            ((DeviceInfo)GatewaySimulator.requestXMLtoObject).setMacAddress(mac);
                                            ((DeviceInfo)GatewaySimulator.requestXMLtoObject).setFirmwareVersion(Wrappers.createStringCap("1.0.0"));
                                            ((DeviceInfo)GatewaySimulator.requestXMLtoObject).setApiVersion(Wrappers.createStringCap("1.7"));
                                        }

                                        equivalent=LocalDataBinder.toString(newResponseObject).contains
                                                (LocalDataBinder.toString(GatewaySimulator.requestXMLtoObject));
                                    }

                                    if (equivalent){
                                        testResult.append("\nPUT succeeded.");
                                        testResultConclusion.append("URI: "+path+" Method: "+method+" Result: "+"Success\n");
                                        success++;
                                    }
                                    else
                                    {testResult.append("\nPUT failed.");
                                        testResultConclusion.append("URI: "+path+" Method: "+method+" Result: "+"Failure"+
                                                " Error: "+"Data GET after PUT didn't match the request.\n");
                                        putError++;
                                    }

                                }


                            }
                            else {
                                putError++;
                                testResultConclusion.append("URI: "+path+" Method: "+method+" Result: "+"Failure\n"+
                                        " Error: "+ "Response returned is not OK response.\n");
                            }

                        }
                    }
                    method="POST";
                    if (simulator.supportMethodForPath(method,endpointPath)){
                        if (!simulator.supportMethodForPath("PUT",endpointPath)){
                            StringBuilder responseSB = new StringBuilder();
                            processRequest(method,path,cameraURL,responseSB,requestBody,null,null,null);
                            testResult.append("\n============================================" +
                                    "\n" + "URI: " + path +
                                    "\nMethod: " + method
                                    + "\n" + "Response: \n\n" +
                                    Utilities.prettyFormat(responseSB.toString()) + "\n" +
                                    "============================================"
                            );

                            String response=Utilities.removeSpacesXml(responseSB.toString());
                            StringReader source=new StringReader(response);
                            Document document=Utilities.getDocument(source);
                            Object responseObject= LocalDataBinder.valueOf(document);
                            if (((ResponseStatus)responseObject).getStatusCode()==1||
                                    ((ResponseStatus)responseObject).getStatusCode()==7){
                                success++;
                                testResultConclusion.append("URI: "+path+" Method: "+method+" Result: "+"Success\n");
                            }
                            else {
                                postError++;
                                testResultConclusion.append("URI: "+path+" Method: "+method+" Result: "+"Failure\n"+
                                        " Error: "+ "Response returned is not OK response.\n");
                            }
                        }
                        else{
                            skip++;
                            testResult.append("\n" +
                                    "============================================" +
                                    "\n"+"URI: "+path+
                                    "\nMethod: POST"
                                    +"\nNo example request provided."
                                    +"\n"+"Skipped"+"\n" +
                                    "============================================"
                            );
                            testResultConclusion.append("URI: "+path+" Method: "+"POST"+" Result: "+"Skipped"
                                    +" Reason: No example request provided.\n");                    }

                    }

                    method="DELETE";
                    if (simulator.supportMethodForPath(method,endpointPath)){
                        StringBuilder responseSB = new StringBuilder();
                        processRequest(method,path,cameraURL,responseSB,requestBody,null,null,null);
                        testResult.append("\n============================================" +
                                "\n"+"URI: "+path+
                                "\nMethod: "+method
                                +"\nRequest body: "+
                                "\n"+Utilities.prettyFormat(requestBody)
                                +"\n"+"Response: \n\n"+
                                Utilities.prettyFormat(responseSB.toString())+"\n"+
                                ""
                        );
                        String response=Utilities.removeSpacesXml(responseSB.toString());
                        StringReader source=new StringReader(response);
                        Document document=Utilities.getDocument(source);
                        Object responseObject= LocalDataBinder.valueOf(document);

                        if (((ResponseStatus)responseObject).getStatusCode()==1){
                            success++;
                            testResultConclusion.append("URI: "+path+" Method: "+method+" Result: "+"Success\n");
                        }
                        else {
                            deleteError++;
                            testResultConclusion.append("URI: "+path+" Method: "+method+" Result: "+"Failure\n"+
                                    " Error: "+ "Response returned is not OK response.\n");
                        }

                    }
                }

                catch (Exception e){
                    testResult=testResult.append("\n" +
                            "====================="+
                            "\nException thrown at " +"URI: "+path+
                            "\nMethod: "+ method
                            +"\n"+"Error: " +
                            "\n"+e +"\n"+
                            "=====================");
                    testResultConclusion.append("URI: "+path+" Method: "+ method+" Result: "+"Failure"+" Error: "
                            +"HTTP request failed.\n");
                    httpError++;

                }
            }
            return  "Success: "+success+
                    "\nSkips: "+skip+
                    "\nErrors found:"+
                    "\n    Http request failures: "+httpError+"" +
                    "\n    GET failures - Response is empty "+getError+
                    "\n    PUT failures - Response is nnt OK response or returned data by GET didn't show PUT request: "+putError+
                    "\n    POST failures - Response is not OK response: "+postError+
                    "\n    DELETE failures - Response is not OK response: "+deleteError+
                    "\n    Unknown errors: "+unknownError+ "\n\n"+
                    testResultConclusion+"\n\n\nDetails: \n"+testResult.toString();
        }

        return "";
    }
    */

}


