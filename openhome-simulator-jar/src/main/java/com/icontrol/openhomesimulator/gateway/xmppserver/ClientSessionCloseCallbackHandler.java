package com.icontrol.openhomesimulator.gateway.xmppserver;

import com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory;
import org.jivesoftware.openfire.session.ClientSessionCloseCallback;
import org.jivesoftware.openfire.session.LocalClientSession;

public class ClientSessionCloseCallbackHandler implements ClientSessionCloseCallback {
    public void process(LocalClientSession session)
    {
        //JID jid = session.getAddress();

        GatewaySimulatorFactory.getInstance().notifyDisconnected(session);
    }
}
