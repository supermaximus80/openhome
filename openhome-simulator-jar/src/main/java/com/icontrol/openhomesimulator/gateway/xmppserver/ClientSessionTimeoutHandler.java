package com.icontrol.openhomesimulator.gateway.xmppserver;

import com.icontrol.openhomesimulator.util.OpenHomeProperties;
import org.jivesoftware.openfire.session.ClientSessionTimeoutConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;


public class ClientSessionTimeoutHandler implements ClientSessionTimeoutConfig {

    private static final Logger log = LoggerFactory.getLogger(ClientSessionTimeoutHandler.class);
    private static final int DEFAULT_TIMEOUT = 30*60;      // in sec. default to 30 min

    static int timeout = DEFAULT_TIMEOUT;
    static {
        String timeoutStr = OpenHomeProperties.getProperty("timeout.clientsession", Long.toString(DEFAULT_TIMEOUT));
        timeout = Integer.parseInt(timeoutStr);
    }

    static public int getTimeoutDefault() {
        return timeout;
    }

    // Todo: (RBB) How is this different or same as "xmpp.client.idle" property?
    // Todo: (RBB) Need to see how this XMPP Server handle keep alives assuming it does?

    public int getTimeout(JID jid)
    {
        log.debug("ClientSessionTimeoutHandler - getTimeout for: "+jid.toString() + " timeout= " + timeout);
        return timeout;
    }
}
