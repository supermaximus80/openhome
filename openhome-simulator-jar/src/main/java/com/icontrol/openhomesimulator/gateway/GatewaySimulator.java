package com.icontrol.openhomesimulator.gateway;

import com.icontrol.openhomesimulator.camera.History;
import com.icontrol.openhomesimulator.util.*;
import com.icontrol.rest.framework.*;
import com.icontrol.openhome.data.*;
import com.icontrol.openhomesimulator.gateway.xmppserver.ServerIQResponseParser;
import com.icontrol.openhomesimulator.gateway.xmppserver.ServerRequestIQ;
import com.icontrol.openhomesimulator.gateway.xmppserver.ServerRequestIQParser;
import org.apache.commons.codec.binary.Base64;
import org.jivesoftware.openfire.PacketDeliverer;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class GatewaySimulator {

    /*
        static variables and methods
     */

    private static final Logger log = LoggerFactory.getLogger(GatewaySimulator.class);

    private static int maxResponseTimeout = 10000;   // 10 sec

    public static Object requestXMLtoObject;

    static {
        String maxResponseTimeoutStr = OpenHomeProperties.getProperty("timeout.response", "10");
        if (maxResponseTimeoutStr != null) {
            maxResponseTimeout = Integer.parseInt(maxResponseTimeoutStr)*1000;
        }
    }

    /*
        internal variables
     */
    SSLSocketFactory factory = null;
    XMPPServer xmppServer;
    JID xmppToAddress;
    PacketDeliverer deliverer;
    boolean bWaiting4Response;
    ServerIQResponseParser cameraIQResponse;
    String expectResponsePacketID;

    public GatewaySimulator(String cameraURL, XMPPServer xmppServer) throws Exception {
        factory = Utilities.getClientSideSSLSocketFactory();

        this.xmppServer = xmppServer;

        xmppToAddress = null;
        deliverer = (xmppServer != null) ? xmppServer.getPacketDeliverer() : null;
        bWaiting4Response = false;
        cameraIQResponse = null;
        expectResponsePacketID = null;
    }

    public int process(String method, String cmdURI, String cameraURL, StringBuilder responseSB, String requestBody, AuthenticationInfo authInfo) throws Exception {

        log.debug("Camera URL: " + cameraURL);

        // determine if xmpp or HTTP
        if (cameraURL.contains("@"))
            return processXMPP(method, cmdURI, cameraURL, responseSB, requestBody, authInfo);
        else
            return processHTTP(method, cmdURI, cameraURL, responseSB, requestBody, authInfo);
    }

    public int processXMPP(String method, String cmdURI, String cameraURL, StringBuilder responseSB, String requestBody, AuthenticationInfo authInfo) throws IOException {
        //find session base on cameraURL
        if (xmppToAddress == null)
            xmppToAddress = findXmppToAddress(cameraURL);

        log.debug("GatewaySimulator - sending over XMPP request to:"+xmppToAddress+" method="+method+" action="+cmdURI);
        ServerRequestIQ requestIQ = new ServerRequestIQ(null, xmppToAddress, method, cmdURI, null, requestBody);

        try {
            bWaiting4Response = true;
            expectResponsePacketID = requestIQ.getID();
            deliverer.deliver(requestIQ);

            // wait for response
            long endTime = System.currentTimeMillis() + maxResponseTimeout;
            while (bWaiting4Response) {
                if (System.currentTimeMillis() >= endTime) {
                    break;
                }
                Thread.sleep(100);
            }
        } catch (Exception ex) {
            log.debug("GatewaySimulator - failed to process request over over XMPP to:"+xmppToAddress+" method="+method+" action="+cmdURI + " - error: " + ex.getMessage());
        } finally {
            bWaiting4Response = false;
            expectResponsePacketID = null;
        }

        // return result
        int retCode;
        if (cameraIQResponse == null) {
            log.error("Timed Out waiting for response");
            responseSB.append("Timed Out waiting for response");
            retCode = 500;
        } else {
            responseSB.append(cameraIQResponse.getBodyText());
            retCode = cameraIQResponse.getCode();
        }

        log.debug("GatewaySimulator - sent over XMPP request to:"+xmppToAddress+" method="+method+" action="+cmdURI + " - code: " + retCode);
        return retCode;

    }

    public void disconnectXMPP() {
        // TODO
    }

    public boolean isXmppCnnected(String cameraURL) {
        JID jid = null;
        try {
            jid = findXmppToAddress(cameraURL);
        } catch (Exception ex) {
            log.error("isXmppCnnected url="+cameraURL+" caught "+ex);
        }
        return (jid != null);
    }

    /*
        Called from different thread than processXMPP
     */
    public void notifyXmppReceivePacket(ServerIQResponseParser iqResponse) {
        if (!bWaiting4Response || expectResponsePacketID==null)
            return;
        // check if to address matches
        if (!expectResponsePacketID.equalsIgnoreCase(iqResponse.getPacketID())) {
            log.debug("notifyXmppReceivePacket ignored packet id="+iqResponse.getPacketID()+" lookingFor="+expectResponsePacketID);
            return;
        }

        bWaiting4Response = false;
        expectResponsePacketID = null;
        cameraIQResponse = iqResponse;
    }

    /*
        Called from different thread than processXMPP
     */
    public void notifyXmppRequestPacket(ServerRequestIQParser iqRequest) {
        try {

            JID from = iqRequest.getIq().getFrom();

            // should be branching off action instead of bodyText, but for simplicity, use bodyText for now
            String bodyText = iqRequest.getBodyText();


            if (bodyText == null) {
                log.debug("Received notifyXmppRequestPacket from "+from+" method:"+iqRequest.getMethod()+" action:"+iqRequest.getAction()+" packetID:"+iqRequest.getIq().getID()+" body:"+bodyText);
                return;
            }




            // remove <?xml ?>, if any
            int pos1 = bodyText.indexOf("<?xml");
            if (pos1 != -1) {
                int pos2 = bodyText.indexOf("?>", pos1);
                if (pos2 != -1)
                    bodyText = bodyText.substring(0, pos1) + bodyText.substring(pos2+"?>".length()) ;
            }

            String trimedBodyText = bodyText.trim();
            if (trimedBodyText.startsWith("<EventAlert")) {
                EventAlert eventAlert = (EventAlert) RestClient.getObject(bodyText,RestConstants.ContentType.TEXT_XML,EventAlert.class);
                GatewaySimulatorFactory.getInstance().storeNotification("eventAlert"+eventAlert.getId().getValue(), "MotionDetectionAlert. state="+eventAlert.getEventState().getValue(), "alert");
            } else if (trimedBodyText.startsWith("<MediaUploadFailure")) {
                MediaUploadEvent mediaUploadFailure = (MediaUploadEvent) RestClient.getObject(bodyText,RestConstants.ContentType.TEXT_XML,MediaUploadEvent.class);
                GatewaySimulatorFactory.getInstance().storeNotification("mediaUploadFailure"+mediaUploadFailure.getId().getValue(), "MediaUpload Failed. type="+mediaUploadFailure.getUploadType().name(), "alert");
            } else if (trimedBodyText.startsWith("<logData")) {
                log.debug("Rx <logData> via XMPP:"+trimedBodyText);
            } else {
                log.debug("Received notifyXmppRequestPacket from "+from+" method:"+iqRequest.getMethod()+" action:"+iqRequest.getAction()+" packetID:"+iqRequest.getIq().getID()+" body:"+trimedBodyText);
            }
        } catch (Exception ex) {
            log.error("notifyXmppRequestPacket caught "+ex, ex);
        }
    }

    private JID findXmppToAddress(String clientJid) throws IOException {
        SessionManager sessionManager = xmppServer.getSessionManager();
        Collection<ClientSession> sessions = sessionManager.getSessions();
        Iterator<ClientSession> iter = sessions.iterator();
        while(iter.hasNext()) {
            ClientSession s = iter.next();
            JID jid = s.getAddress();
            if (clientJid.equalsIgnoreCase(jid.toString())) {
                //log.debug("Found XMPP client address JID: "+clientJid);
                return jid;
            }
        }

        log.error("Failed to find XMPP client address JID: "+clientJid);
        throw new IOException("Failed to find XMPP client address JID: "+clientJid);
    }

    public int processHTTP(String method, String cmdURI, String cameraURL, StringBuilder responseSB, String requestBody, AuthenticationInfo authInfo) {
        RestClient restClient=new HttpRestClient(cameraURL);
        int retCode;
        //custom headers with Content-type and Authorization. Values are null initially.
        String [][] headers=new String [2][];
        String [] contentType={"Content-Type",null};
        String [] auth = {"Authorization",null};
        headers[0]=auth;
        headers [1]=contentType;
        //create Authorization header
        if (authInfo!=null){
            String encoded = new String(Base64.encodeBase64((authInfo.getUsername() + ":" + authInfo.getPassword()).getBytes()));
            auth[1]= "Basic "+encoded;
        }
        try{
            RestClient.Response response=null;
            //GET and DELETE method  - request body always null; or when request body is empty
            if (method.equals("GET")||method.equals("DELETE")||requestBody.equals("")){
                response = restClient.invoke(method,false,cmdURI,null,headers,null);
            }
            //PUT and POST method
            else if (method.equals("PUT") || method.equals("POST")){
                if (requestBody==null || !requestBody.startsWith("<?xml")){
                    response=restClient.invoke(method,false,cmdURI,null,headers,requestBody);
                }
                else {
                    requestBody=Utilities.getCleanXml(requestBody);
                    byte[] data = requestBody.getBytes();
                    //add Content-type as text/xml
                    contentType[1] = RestConstants.ContentType.TEXT_XML;
                    response=restClient.invoke(method,false,cmdURI,null,headers,data);
                }
            }
            if (response.getStatus() !=200){
                System.out.println(method+" to uriStr:"+cmdURI+" error. statusCode:"+response.getStatus());
                retCode= response.getStatus();
            }
            else  {
                String re=response.getContentAsString();
                responseSB.append(re);
                retCode= response.getStatus();
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            retCode=-1;
        }
        if (retCode!=200){
            responseSB.append("Failure to complete request");
        }
        //log history
        History.CommandEvent event = History.getInstance().createCommandEvent(cmdURI, new Date(), new Date(), 0);
        event.setResponseCode(retCode);
        History.getInstance().add(event);

        return retCode;
    }
}
