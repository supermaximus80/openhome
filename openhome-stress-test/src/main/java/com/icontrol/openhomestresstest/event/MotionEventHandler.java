package com.icontrol.openhomestresstest.event;

import com.icontrol.openhomesimulator.camera.CameraSimulator;
import com.icontrol.openhomesimulator.camera.EventAlertMessage;
import com.icontrol.openhomestresstest.StressTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;

public class MotionEventHandler extends EventHandler {

    private static final Logger log = LoggerFactory.getLogger(MotionEventHandler.class);

    private static Random random = new Random();

    private String siteId = null;
    private String serialNo = null;
    private StressTester parent = null;

    private long notifyIndex = 0;
    private String notifyIdPrefix = "";
    private String packetIdIdPrefix = "";

    private boolean enabled;
    private long notifyInterval;
    private long triggerInterval;
    private long nextNotifyTime;
    private long nextTriggerTime;
    private boolean nextNotifyState;

    /*
        init
     */
    public void init(String siteId, String serialNo, StressTester parent, Properties properties) {
        this.siteId = siteId;
        this.serialNo = serialNo;
        this.parent = parent;
        this.notifyIndex = 1;
        this.notifyIdPrefix = serialNo + "-";
        this.packetIdIdPrefix = (siteId != null ? siteId.substring(siteId.length()-6) : "") + StressTester.MOTION_ALERT_PACKETID_SUFFIX;

        try {
            this.enabled = "true".equalsIgnoreCase(properties.getProperty("motionDetection.enabled", "false"));
            this.notifyInterval = 1000 * Integer.parseInt(properties.getProperty("motionDetection.notify.intervalBetweenEvents", "120"));
            this.triggerInterval = 1000 * Integer.parseInt(properties.getProperty("motionDetection.trigger.intervalBetweenEvents", "300"));
        } catch (java.lang.NumberFormatException ex) {
            log.warn("MotionEventHandler caught error. Use default settings:"+ex);
            this.enabled = false;
            this.notifyInterval = 120 * 1000;
            this.triggerInterval = 300 * 1000;
        }

        /*
            Note:
            notifyInterval is interval between notifications for active/non-active events in ms
            triggerInterval is interval between a simulator motion event in ms
         */

        // calculate next notify time
        nextNotifyState = false;
        if (enabled) {
            long now = System.currentTimeMillis();
            nextNotifyTime = now + (long) (triggerInterval*random.nextFloat()) + notifyInterval;
            nextTriggerTime = now + triggerInterval;
        } else {
            nextNotifyTime = -1;
            nextTriggerTime = -1;
        }
    }

    public void run(CameraSimulator cameraSimulator) {
        if (isStopped || !enabled)
            return;

        // is time to send motion notification?
        try {
            long now = System.currentTimeMillis();

            if ( timeToSendMotionEvent(now) )  {
                String uri = "xmpp://" + cameraSimulator.getXmppGw() + "/eventalert";
                String xmlBody = EventAlertMessage.createMessagePIR(notifyIdPrefix + Long.toString(notifyIndex), nextNotifyState);
                notifyIndex++;

                // setup next notify time/state
                prepareNextMotionState();

                if (!sendNotificationAndVerifyResponse(cameraSimulator, "POST", uri, xmlBody, packetIdIdPrefix) )
                    log.error("sendNotificationAndVerifyResponse failed for siteId:"+siteId +" serialNo:"+serialNo);
                else
                    log.info("Send Motion notification. Serial:"+serialNo+" state:"+!nextNotifyState+" index:"+notifyIndex);
            }
        } catch (Exception ex) {
            log.error("MotionEventHandler.run caught "+ex, ex);
        }

       // check for response timeout
        super.checkResponseTimeout();
    }

    public void resetStats() {
        sumTimeOfEvents = 0;
        totalNumberOfEventSent = 0;
    }

    private boolean timeToSendMotionEvent(long now) {
        return (now > nextNotifyTime) ;
    }

    private void prepareNextMotionState() {
        if (enabled && nextTriggerTime > 0) {
            long now = System.currentTimeMillis();
            // setup next trigger time if we just sent active
            while (nextTriggerTime <= now)
                nextTriggerTime += triggerInterval;

            // flip state - for this simple test
            nextNotifyState = !nextNotifyState;

            if (nextNotifyState == false)      // if next state is inactive, schedule base on notify interval
                nextNotifyTime = now + notifyInterval;
            else                              // else notify a new active state at next trigger interval
                nextNotifyTime = nextTriggerTime;

            if (nextNotifyTime < (now+notifyInterval) ) {
                nextNotifyTime = now + notifyInterval;
                log.warn("MotionEventHandler.prepareNextMotionState reset notify time");
            }
        }
    }

}
