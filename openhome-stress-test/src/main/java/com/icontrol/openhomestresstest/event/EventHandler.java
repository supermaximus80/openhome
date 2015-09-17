package com.icontrol.openhomestresstest.event;

import com.icontrol.openhomesimulator.camera.CameraSimulator;
import com.icontrol.openhomesimulator.camera.xmppclient.OpenHomeResponseIQ;
import com.icontrol.openhomestresstest.StressTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


public abstract class EventHandler {

    private static final Logger log = LoggerFactory.getLogger(MotionEventHandler.class);

    protected long max_wait4responseQueue = 1;

    protected long totalNumberOfEventSent = 0;
    protected long sumTimeOfEvents = 0;
    protected long eventsSent = 0;
    protected long total200OKResponses = 0;
    protected long totalNon200OKResponses = 0;
    protected long totalDiscardSendDueToMaxTransmitQueue = 0;
    protected long numMaxTransmitQueueResets = 0;


    protected boolean isStopped = false;

    protected Map<String, Long> waitForResponseMap = new ConcurrentHashMap<String, Long>();

    public boolean sendNotificationAndVerifyResponse(CameraSimulator cameraSimulator, final String method, final String uri, final String xmlBody, final String packetIdPrefix) throws IOException {

        // check if camera is connected
        if (!cameraSimulator.isXMPPconnected()) {
            log.error("Error! Notification Not sent. Camera not connected. serialNo:"+cameraSimulator.getSerialNo());
            return false;
        }

        // check if notification response queue is above threshold
        if (waitForResponseMap.size() > max_wait4responseQueue) {
            numMaxTransmitQueueResets++;
            totalDiscardSendDueToMaxTransmitQueue += waitForResponseMap.size();
            log.error("Error! Notification Queue exceeded max:"+waitForResponseMap.size()+" Disconnect camera. serialNo:"+cameraSimulator.getSerialNo());
            waitForResponseMap.clear();
            // disconnect camera
            cameraSimulator.destroy();
            return false;
        }

        // send notification event
        String packetId = packetIdPrefix + Long.toString(totalNumberOfEventSent);
        cameraSimulator.sendXmppNotification(method, uri, xmlBody, packetId);
        waitForResponseMap.put(packetId, System.currentTimeMillis());

        totalNumberOfEventSent++;
        eventsSent++;

        log.debug("sendNotificationAndVerifyResponse serialNo:"+cameraSimulator.getSerialNo()+" sent Notification. URI:"+uri+" packetId:"+packetId);

        return true;
    }

    protected void checkResponseTimeout() {

    }

    public boolean processResponseIQ(OpenHomeResponseIQ iq) {
        try {
            String packetId = iq.getPacketID();
            log.debug("processResponseIQ received packetId:"+packetId+" to:"+iq.getTo());

            // lookup map for pending responses
            Long startTime = waitForResponseMap.get(packetId);
            if (startTime == null) {
                log.error("Unknown pending response for packetId:"+packetId+" iq to:"+iq.getTo()+ " responseCode:"+iq.getCode());
                return false;
            }

            // remove from pending map
            waitForResponseMap.remove(packetId);

            // calculate response time
            long duration = System.currentTimeMillis() - startTime;
            sumTimeOfEvents += duration;

            // record 200 OK or other code
            if (iq.getCode() == 200) {
                total200OKResponses++;
                log.info("Server response with code: "+iq.getCode()+" for packetId:"+packetId + " responseTime:"+duration+" ms");
            } else {
                totalNon200OKResponses++;
                log.warn("Server response with error code: "+iq.getCode()+" for packetId:"+packetId + " responseTime:"+duration+" ms");
            }
            return true;
        } catch (Exception ex) {
            log.error("processResponseIQ caught "+ex, ex);
            return false;
        }
    }


    public abstract void init(String siteId, String serialNo, StressTester parent, Properties properties);

    public abstract void run(CameraSimulator cameraSimulator);

    public long getTotalNumberOfEventSent() {
        return totalNumberOfEventSent;
    }

    public long getSumTimeOfEvents() {
        return sumTimeOfEvents;
    }

    public long getEventsSent() {
        return eventsSent;
    }

    public void resetEventTime() {
        sumTimeOfEvents = 0;
        eventsSent = 0;
    }

    public void stop(boolean isStopped) {
        this.isStopped = isStopped;
    }

}
