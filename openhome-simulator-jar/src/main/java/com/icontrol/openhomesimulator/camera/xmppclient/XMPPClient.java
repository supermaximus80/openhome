package com.icontrol.openhomesimulator.camera.xmppclient;

import com.icontrol.openhomesimulator.camera.CameraSimulatorFactory;
import com.icontrol.openhomesimulator.camera.XmppURL;
import com.icontrol.openhomesimulator.util.Utilities;
import org.jivesoftware.openfire.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public class XMPPClient implements PacketListener {
    private static final Logger log = LoggerFactory.getLogger(XMPPClient.class);

    private SSLSocketFactory factory;

    private String serialNo;
    private String host ;
    private String siteId;
    private String userName;
    private XmppClientIQListener xmppClientIQListener;

    private int port = 5222;
    private String clientAddress;
    private String remoteAddress;
    private String password ;
    private String serviceName ;
    private boolean saslAuthenticationEnabled ;
    protected XMPPConnection[] connections ;
    private boolean isLive;
    boolean isCompressionEnabled = true;
    private int txPacketId = 100;   // only used for client originated messages

    public XMPPClient(String serverHost, int serverPort) throws Exception {

        this.factory = Utilities.getClientSideSSLSocketFactory();

        this.host = serverHost;
        this.port = serverPort;
        this.clientAddress = "";
        this.remoteAddress = "";
        this.password = "";

        this.serviceName = "";
        this.saslAuthenticationEnabled = true;
        this.connections = null;
        this.xmppClientIQListener = null;

        if (host == null)
            host = "localhost";
        if (this.port <= 0) {
            port = 5222;
        }

        isLive = true;

        // add custom IQ provider in order to parse OpenHome IQ
        ProviderManager providerManager = ProviderManager.getInstance();
        providerManager.addIQProvider("http-tunnel", "http://icontrol.com/http-tunnel/v1", new OpenHomeIQProvider());

        // Smac Debug. Set to true to turn ON Smac Debugger.
        // Alernatively, turn debug on via Java JVM parameter -  -Dsmack.debugEnabled=true
        // XMPPConnection.DEBUG_ENABLED = false;

        String policyName = JiveGlobals.getProperty("xmpp.client.compression.policy", Connection.CompressionPolicy.optional.toString());
        log.debug("compression policyName: " +  policyName);

        isCompressionEnabled = !Connection.CompressionPolicy.disabled.toString().equals(policyName);
        log.debug("isCompressionEnabled: " + isCompressionEnabled);
    }

    public void setXmppClientIQListener(XmppClientIQListener xmppClientIQListener) {
        this.xmppClientIQListener = xmppClientIQListener;
    }

    /**
     * Returns a SocketFactory that will be used to create the socket to the XMPP server. By
     * default no SocketFactory is used but subclasses my want to redefine this method.<p>
     *
     * A custom SocketFactory allows fine-grained control of the actual connection to the XMPP
     * server. A typical use for a custom SocketFactory is when connecting through a SOCKS proxy.
     *
     * @return a SocketFactory that will be used to create the socket to the XMPP server.
     */
    protected SocketFactory getSocketFactory() {
        if (!JiveGlobals.getBooleanProperty("xmpp.socket.ssl.active")) {
            return null;
        }
        return factory;
    }

    public int createXMPPConnection() throws Exception {
        if (clientAddress == null)
            throw new IOException("NULL client address.");

        if (connections != null || getMaxConnections() < 1) {      // early return if connections is already setup
            return 200;
        }
        connections = new XMPPConnection[getMaxConnections()];
        // Connect to the server
        for (int i = 0; i < getMaxConnections(); i++) {
            connections[i] = createConnection();
            connections[i].connect();
        }

        // Use the host name that the server reports. This is a good idea in
        // most cases, but could fail if the user set a hostname in their XMPP
        // server that will not resolve as a network connection.
        host = connections[0].getHost();
        serviceName = connections[0].getServiceName();

        for (int i = 0; i < getMaxConnections(); i++) {
            getConnection(i).login(clientAddress, password, siteId, false);
        }

        // Add listener
        //connections[0].addPacketListener(this, new ToContainsFilter(clientAddress));
        connections[0].addPacketListener(this, null);

        // Let the server process the available presences
        //Thread.sleep(150);

        // check username in use
        if (!getConnection(0).getUser().contains(clientAddress)) {
            log.error("Error! Disconnect. Invalid userName:"+getConnection(0).getUser()+" expecting:"+clientAddress);
            getConnection(0).disconnect();
            throw new IOException("Error! Disconnect. Invalid userName:"+getConnection(0).getUser()+" expecting:"+clientAddress) ;
        }

        return 200;
     }

    /**
     * Creates a new XMPPConnection using the connection preferences. This is useful when
     * not using a connection from the connection pool in a test case.
     *
     * @return a new XMPP connection.
     */
    protected XMPPConnection createConnection() {
        // Create the configuration for this new connection
        ConnectionConfiguration config = new ConnectionConfiguration(host, port);
        config.setCompressionEnabled(isCompressionEnabled);
        config.setSASLAuthenticationEnabled(saslAuthenticationEnabled);
        config.setSocketFactory(getSocketFactory());
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);

        return new XMPPConnection(config);
    }

    /**
     * Returns the XMPPConnection located at the requested position. Each test case holds a
     * pool of connections which is initialized while setting up the test case. The maximum
     * number of connections is controlled by the message {@link #getMaxConnections()} which
     * every subclass must implement.<p>
     *
     * If the requested position is greater than the connections size then an
     * IllegalArgumentException will be thrown.
     *
     * @param index the position in the pool of the connection to look for.
     * @return the XMPPConnection located at the requested position.
     */
    protected XMPPConnection getConnection(int index) {
        if (index > getMaxConnections()) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        return connections[index];
    }

    protected int getMaxConnections() {
        return 1;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public void setClientAddress(String serialNo, String siteId) {
        this.serialNo = serialNo;
        this.siteId = siteId;
        this.clientAddress = serialNo + "_" + siteId +"@"+host+"/"+siteId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSASLAuthenticationEnabled(boolean saslAuthenticationEnabled) {
        this.saslAuthenticationEnabled = saslAuthenticationEnabled;
    }

    public boolean isConnected() {
        return (connections != null && connections.length > 0 && connections[0] != null && connections[0].isConnected());
    }

    public void disconnect() {
        if (connections == null)
            return;
        for (int i=0; i<connections.length;i++) {
            connections[i].disconnect();
        }
        connections = null;
        if (xmppClientIQListener != null)
            xmppClientIQListener.disconnected();
    }

    @Override
    public void processPacket(Packet packet) {
        if (packet instanceof OpenHomeRequestIQ) {
            OpenHomeRequestIQ requestIq = (OpenHomeRequestIQ) packet;
            log.debug("XMPPClient Rx RequestIQ: "+requestIq.toXML());
            if (xmppClientIQListener != null) {
                if (xmppClientIQListener.processRequestIQ(requestIq))
                    return;
            }
            // process iq locally
            localDirectRequest(requestIq);
        } else if (packet instanceof OpenHomeResponseIQ) {
            OpenHomeResponseIQ responseIq = (OpenHomeResponseIQ) packet;
            log.debug("XMPPClient Rx ResponseIQ: "+responseIq.toXML());
            if (xmppClientIQListener != null) {
                if (xmppClientIQListener.processResponseIQ(responseIq))
                    return;
            }
        } else {
            log.error("XMPPClient Rx and Ignored Non-OpenHome packet: " + packet.toXML());
        }
    }


    private void localDirectRequest(OpenHomeRequestIQ requestIq) {
        LocalDirectRestRequest request = new LocalDirectRestRequest(log);
        OpenHomeResponseIQ responseIQ = request.process(requestIq) ;
        // set from address
        responseIQ.setFrom(clientAddress);
        if (remoteAddress == null || remoteAddress.length()==0)
            remoteAddress = requestIq.getFrom();
        responseIQ.setTo(requestIq.getFrom());

        // send response back to Gateway
        // log.debug("localProxyRequest - sending response:\n"+responseIQ.toXML());
        getConnection(0).sendPacket(responseIQ);
        log.debug("localDirectRequest - sent response:\n" + responseIQ.toXML());
    }

    /*
    private void localProxyRequest(OpenHomeRequestIQ requestIq) {
        LocalHttpRequest request = new LocalHttpRequest(CameraSimulatorFactory.getInstance().getOpenHomeApiPath(), null, factory, log);
        OpenHomeResponseIQ responseIQ = request.process(requestIq) ;
        // set from address
        responseIQ.setFrom(clientAddress);
        if (remoteAddress == null || remoteAddress.length()==0)
            remoteAddress = requestIq.getFrom();
        responseIQ.setTo(requestIq.getFrom());

        // send response back to Gateway
        // log.debug("localProxyRequest - sending response:\n"+responseIQ.toXML());
        getConnection(0).sendPacket(responseIQ);
        log.debug("localProxyRequest - sent response:\n" + responseIQ.toXML());
    }
    */

    public void sendNotification(String method, String action, String body) throws IOException {
        String packetId = serialNo + Integer.toString(txPacketId++);
        sendNotification(method, action, body, packetId);
    }

    public void sendNotification(String method, String action, String body, String packetId) throws IOException {
        XMPPConnection c = getConnection(0);
        if (c == null)
            throw new IOException("Client not connected");

        OpenHomeRequestIQ requestIQ = new OpenHomeRequestIQ();
        requestIQ.setMethod(method);
        requestIQ.setAction(action);
        if (body != null)
            requestIQ.setBodyText(body);
        requestIQ.setPacketID(packetId);

        // set from address
        requestIQ.setFrom(clientAddress);

        // set to address. get to domain from action
        XmppURL xmppURL = new XmppURL(action);
        requestIQ.setTo("000000000000" + "@" + xmppURL.getHost());

        c.sendPacket(requestIQ);
    }

}
