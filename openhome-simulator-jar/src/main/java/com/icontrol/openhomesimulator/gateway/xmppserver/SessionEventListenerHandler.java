package com.icontrol.openhomesimulator.gateway.xmppserver;

import com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory;
import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.LoggerFactory;

public class SessionEventListenerHandler implements SessionEventListener {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SessionEventListenerHandler.class);

    @Override
    public void sessionCreated(Session session) {
        if (session instanceof ClientSession) {
            log.debug("sessionCreated jid="+session.getAddress());
            GatewaySimulatorFactory.getInstance().notifyNewConnection( (ClientSession) session);
        }
    }

    @Override
    public void sessionDestroyed(Session session) {
        log.debug("sessionDestroyed jid="+session.getAddress());
    }

    @Override
    public void anonymousSessionCreated(Session session) {
        log.debug("anonymousSessionCreated jid="+session.getAddress());
    }

    @Override
    public void anonymousSessionDestroyed(Session session) {
        log.debug("anonymousSessionDestroyed jid="+session.getAddress());
    }
}
