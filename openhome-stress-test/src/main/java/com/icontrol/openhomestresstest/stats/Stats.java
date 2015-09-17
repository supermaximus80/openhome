package com.icontrol.openhomestresstest.stats;

import org.slf4j.LoggerFactory;

public class Stats {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Stats.class);

    public long numActiveXMPPconnections;
    public long numXMPPconnects;
    public long totXMPPconnectTime;
    public long numXMPPconnectFailures;
    public long numMotionEvents;
    public long totMotionTime;
    public long startTime;


    public Stats() {
        reset();
        startTime = System.currentTimeMillis();
    }

    public void reset() {
        numActiveXMPPconnections = 0;
        numXMPPconnects = 0;
        totXMPPconnectTime = 0;
        numXMPPconnectFailures = 0;
        numMotionEvents = 0;
        totMotionTime = 0;
    }

    public void AddStats(Stats sum) {
        sum.numActiveXMPPconnections += numActiveXMPPconnections;
        sum.numXMPPconnects += numXMPPconnects;
        sum.totXMPPconnectTime += totXMPPconnectTime;
        sum.numXMPPconnectFailures += numXMPPconnectFailures;
        sum.numMotionEvents += numMotionEvents;
        sum.totMotionTime += totMotionTime;
    }

    public void print_header() {
        logger.info(", Statistics: time(sec), #Sessions, #reconnects, avgReconnectTime(ms), #connectFailures, #MotionEvents, avgResponseTime(ms)");
    }

    public void print_report() {
        StringBuilder sb = new StringBuilder();
        sb.append(", Statistics: ").append(Long.toString((System.currentTimeMillis()-startTime)/1000));
        sb.append(", ").append(Long.toString(numActiveXMPPconnections));
        sb.append(", ").append(Long.toString(numXMPPconnects));
        sb.append(", ").append(Long.toString(numXMPPconnects>0?(totXMPPconnectTime/numXMPPconnects):0));
        sb.append(", ").append(Long.toString(numXMPPconnectFailures));
        sb.append(", ").append(Long.toString(numMotionEvents));
        sb.append(", ").append(Long.toString(numMotionEvents>0?(totMotionTime/numMotionEvents):0));

        logger.info(sb.toString());
     }
}
