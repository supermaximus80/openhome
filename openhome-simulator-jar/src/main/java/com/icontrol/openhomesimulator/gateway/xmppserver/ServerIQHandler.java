package com.icontrol.openhomesimulator.gateway.xmppserver;

import com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.disco.*;
import org.jivesoftware.openfire.forms.DataForm;
import org.jivesoftware.openfire.forms.FormField;
import org.jivesoftware.openfire.forms.spi.XDataFormImpl;
import org.jivesoftware.openfire.forms.spi.XFormFieldImpl;
import org.jivesoftware.openfire.handler.IQHandler;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ServerIQHandler extends IQHandler implements ServerFeaturesProvider,
        DiscoInfoProvider, DiscoItemsProvider
{
    protected static final Logger log = LoggerFactory.getLogger(ServerIQHandler.class);

    private static final String NAMESPACE = "http://icontrol.com/http-tunnel/v1";
    private IQHandlerInfo info;
    private IQDiscoInfoHandler infoHandler;
    private IQDiscoItemsHandler itemsHandler;

    public ServerIQHandler()
    {
        super("Message Handler");
        info = new IQHandlerInfo("http", NAMESPACE);
    }


    public IQ handleIQ(IQ packet)
    {
        //System.out.println("IQHandler.handleIQ packet:"+packet.toXML());
        try {
            try {
                ServerIQResponseParser responseIQ = new ServerIQResponseParser(packet);

                // notify Gateway of incoming response packet
                log.debug("ServerIQHandler - Notifying IQ Response packet: " + responseIQ);
                GatewaySimulatorFactory.getInstance().notifyXmppReceivePacket(responseIQ);
                return new IQ();
            } catch (Exception ex) {
                ServerRequestIQParser requestIQ = new ServerRequestIQParser(packet);
                // notify Gateway of incoming request packet
                log.debug("ServerIQHandler - Notifying IQ Request packet: " + requestIQ);
                GatewaySimulatorFactory.getInstance().notifyXmppRequestPacket(requestIQ);
                return new IQ();
            }
        } catch (Exception ex) {
            log.error("ServerIQHandler - Failed to handleIQ: caught "+ex.getMessage());
            IQ response = IQ.createResultIQ(packet);
            PacketError pe = new PacketError(PacketError.Condition.internal_server_error, PacketError.Type.cancel, "test error");
            response.setType(IQ.Type.error);
            response.setChildElement(pe.getElement());
            return response;
        }
    }

    public IQHandlerInfo getInfo() {
        return info;
    }

    public Iterator<String> getFeatures() {
        ArrayList<String> features = new ArrayList<String>();
        features.add(NAMESPACE);
        return features.iterator();
    }

    public Iterator<Element> getIdentities(String name, String node, JID senderJID) {
        ArrayList<Element> identities = new ArrayList<Element>();
        Element identity = DocumentHelper.createElement("identity");
        identity.addAttribute("category", "automation");
        identity.addAttribute("type", "message-list");
        identities.add(identity);
        return identities.iterator();
    }

    public Iterator<String> getFeatures(String name, String node, JID senderJID) {
        return Arrays.asList(NAMESPACE).iterator();
    }

    public XDataFormImpl getExtendedInfo(String name, String node, JID senderJID) {
        // Mark that offline messages shouldn't be sent when the user becomes available
        //stopOfflineFlooding(senderJID);

        XDataFormImpl dataForm = new XDataFormImpl(DataForm.TYPE_RESULT);

        XFormFieldImpl field = new XFormFieldImpl("FORM_TYPE");
        field.setType(FormField.TYPE_HIDDEN);
        field.addValue(NAMESPACE);
        dataForm.addField(field);

        field = new XFormFieldImpl("number_of_messages");
        //field.addValue(String.valueOf(messageStore.getMessages(senderJID.getNode(), false).size()));
        dataForm.addField(field);

        return dataForm;
    }

    public boolean hasInfo(String name, String node, JID senderJID)
    {
//        return NAMESPACE.equals(node) && userManager.isRegisteredUser(senderJID.getNode());
    	return NAMESPACE.equals(node);
    }

    public Iterator<DiscoItem> getItems(String name, String node, JID senderJID) {
        // Mark that offline messages shouldn't be sent when the user becomes available
        //stopOfflineFlooding(senderJID);
        List<DiscoItem> answer = new ArrayList<DiscoItem>();
//        for (OfflineMessage offlineMessage : messageStore.getMessages(senderJID.getNode(), false)) {
//            synchronized (dateFormat) {
//                answer.add(new DiscoItem(new JID(senderJID.toBareJID()), offlineMessage.getFrom().toString(), dateFormat.format(offlineMessage.getCreationDate()), null));
//            }
//        }

        return answer.iterator();
    }

    public void initialize(XMPPServer server) {
        super.initialize(server);
        infoHandler = server.getIQDiscoInfoHandler();
        itemsHandler = server.getIQDiscoItemsHandler();
//        messageStore = server.getOfflineMessageStore();
//        sessionManager = server.getSessionManager();
//        userManager = server.getUserManager();
//        routingTable = server.getRoutingTable();
    }

    public void start() throws IllegalStateException {
        super.start();
        infoHandler.setServerNodeInfoProvider(NAMESPACE, this);
        itemsHandler.setServerNodeInfoProvider(NAMESPACE, this);
    }

    public void stop() {
        super.stop();
        infoHandler.removeServerNodeInfoProvider(NAMESPACE);
        itemsHandler.removeServerNodeInfoProvider(NAMESPACE);
    }
}
