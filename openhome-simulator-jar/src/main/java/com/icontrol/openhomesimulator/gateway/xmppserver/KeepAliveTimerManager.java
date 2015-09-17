package com.icontrol.openhomesimulator.gateway.xmppserver;

import com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory;
import org.jivesoftware.openfire.StreamID;
import org.jivesoftware.openfire.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class KeepAliveTimerManager {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveTimerManager.class);

    private static final int KEEP_ALIVE_CHECK_TIMER_THREADS_DEFAULT = 2;


    private String method;
    private String commandURI;
    private String body;
    private ScheduledThreadPoolExecutor keepAliveTimerPool;
    private ConcurrentHashMap<String, Long> streamIDmap;

    public KeepAliveTimerManager() {
        method = "GET";
        commandURI = "/OpenHome/System/Ping";
        body = null;

        keepAliveTimerPool = new ScheduledThreadPoolExecutor(KEEP_ALIVE_CHECK_TIMER_THREADS_DEFAULT);
        streamIDmap = new ConcurrentHashMap<String, Long>();
    }

    public void close() {
        keepAliveTimerPool.shutdownNow();
        streamIDmap.clear();
    }


    public void addConnection(ClientSession cs, long timeoutSec) throws Exception {
        if (cs == null)
            return;

        // check for existing streamID
        if (hasStreamID(cs.getStreamID()))
            return;

        long keepAlivetimeMs = timeoutSec * 1000 ;
        if (keepAlivetimeMs < 1000) {    // if < 1 sec, do not run keep alive timer
            return;
        }

        // reserve headroom for timer lags
        keepAlivetimeMs -= 500;

        // track stream started
        streamStarted(cs.getStreamID());

        // start timer
        KeepAliveTask task = new KeepAliveTask(cs, keepAlivetimeMs);
        keepAliveTimerPool.schedule(task, keepAlivetimeMs, TimeUnit.MILLISECONDS);

        log.debug("KeepAliveTimerManager.addConnection jid="+cs.getUsername()+" streamID:"+cs.getStreamID()+" timeoutSec:"+timeoutSec);
    }

    public void addConnection(String jid, long timeoutSec) throws Exception {
        if (jid == null)
            return;

        long keepAlivetimeMs = timeoutSec * 1000 ;
        if (keepAlivetimeMs < 5000) {    // if < 5 sec, do not run keep alive timer
            return;
        }

        // reserve headroom for timer lags
        keepAlivetimeMs -= 2000;

        // kick start 1 timer to look for valid ClientSession
        KeepAliveTask task = new KeepAliveTask(jid, keepAlivetimeMs);
        keepAliveTimerPool.schedule(task, 50, TimeUnit.MILLISECONDS);
    }

    public void removeConnection(ClientSession cs) {
        // do nothing for now since timer automatically stops itself if session no longer active
    }

    // track finished streamID
    private void streamStarted(StreamID streamID) {
        if (streamID != null)
            streamIDmap.put(streamID.getID(), new Long(System.currentTimeMillis()));
    }

    private void streamEnded(StreamID streamID) {
        if (streamID != null)
            streamIDmap.remove(streamID.getID());
    }

    private boolean hasStreamID(StreamID streamID) {
        if (streamID == null)
            return true;
        return streamIDmap.get(streamID.getID()) != null ;
    }

    class KeepAliveTask extends TimerTask {
        ClientSession clientSession;
        long keepAliveMs;
        String jid;

        KeepAliveTask(ClientSession cs, long keepAliveMs) throws IOException  {
            if (cs == null || keepAliveMs <= 0)
                throw new IOException("KeepAliveTask Invalid arg");
            this.clientSession = cs;
            this.keepAliveMs = keepAliveMs;
            this.jid = null;
        }

        KeepAliveTask(String jid, long keepAliveMs) throws IOException  {
            if (jid == null || keepAliveMs <= 0)
                throw new IOException("KeepAliveTask Invalid arg");
            this.clientSession = null;
            this.keepAliveMs = keepAliveMs;
            this.jid = jid;
        }

        public void run() {
            try {
                boolean bFirstTime = false;

                // get clientSession if null
                if (clientSession == null) {
                    clientSession = GatewaySimulatorFactory.getInstance().getXmppClientSession(new JID(jid));
                    if (clientSession == null) {
                        if (log.isDebugEnabled())
                            log.debug("KeepAliveTask unable to find client session for JID:"+jid);
                        return;
                    } else if (hasStreamID(clientSession.getStreamID())) {
                        if (log.isDebugEnabled())
                            log.debug("KeepAliveTask redundant streamID:"+clientSession.getStreamID().getID()+" for JID:"+jid);
                        return;
                    } else {
                        streamStarted(clientSession.getStreamID());
                        bFirstTime = true;
                        if (log.isDebugEnabled())
                            log.debug("KeepAliveTask start keep-alive timer for JID:"+jid+" streamID:"+clientSession.getStreamID().getID()+" keepAlivetTimeMs:"+keepAliveMs);
                    }
                } else if (jid == null) {
                    jid = clientSession.getAddress().toString();
                }

                // check if connection is already idle
                if (clientSession.getStatus() < ClientSession.STATUS_CONNECTED) {
                    streamEnded(clientSession.getStreamID());
                    if (log.isDebugEnabled())
                        log.debug("KeepAliveTask finished keep-alive check for streamID:"+clientSession.getStreamID()+" since connection no longer active. status:"+clientSession.getStatus());
                    return;
                }

                // send XMPP keep alive message to camera, throws exception if session is disconnected/closed
                if (!bFirstTime)
                    GatewaySimulatorFactory.getInstance().sendRequestIQ(null, clientSession, method, commandURI, null, body);

                // schedule next timer
                KeepAliveTask task = new KeepAliveTask(clientSession, keepAliveMs);
                keepAliveTimerPool.schedule(task, keepAliveMs, TimeUnit.MILLISECONDS);

            } catch (Exception ex) {
                streamEnded(clientSession.getStreamID());
                log.error("KeepAliveTask.run streamID:" + clientSession.getStreamID() +" jid:"+jid + " caught " + ex);
            }
        }
    }

}
