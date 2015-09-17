package com.icontrol.openhomestresstest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;


public class RetryTracker {
    private static final Logger log = LoggerFactory.getLogger(RetryTracker.class);

    private static Random random = new Random();

    private long minWait, maxWait, stepSize, maxRetries;
    private long curRetry;
    private long nextRetryTime, lastRetryTime;

    public RetryTracker(Properties properties) {
        try {
            this.minWait = Integer.parseInt(properties.getProperty("xmpp.client.retry.minWait", "1000"));
            this.maxWait = Integer.parseInt(properties.getProperty("xmpp.client.retry.maxWait", "120000"));
            this.stepSize = Integer.parseInt(properties.getProperty("xmpp.client.retry.stepSize", "2000"));
            this.maxRetries = Integer.parseInt(properties.getProperty("xmpp.client.retry.maxRetries", "-1"));
        } catch (java.lang.NumberFormatException ex) {
            log.error("RetryTracker caught error. Use default settings:"+ex);
            this.minWait = 1000;
            this.maxWait = 120000;
            this.stepSize = 2000;
            this.maxRetries = -1;
        }

        reset();
    }

    public RetryTracker(int minWait, int maxWait, int stepSize, int maxRetries) {
        this.minWait = minWait;
        this.maxWait = maxWait;
        this.stepSize = stepSize;
        this.maxRetries = maxRetries;

        reset();
    }

    void reset() {
        curRetry = 0;
        lastRetryTime = System.currentTimeMillis();
        nextRetryTime = -1;
    }

    boolean timeForNextRetry() {
        return timeForNextRetry(System.currentTimeMillis());
    }

    boolean timeForNextRetry(long now) {
        if (maxRetries > 0 && curRetry > maxRetries)
            return false;

        if (nextRetryTime < 0) {
            if (curRetry == 0)
                nextRetryTime = lastRetryTime + minWait + (long)(stepSize * random.nextFloat());
            else
                nextRetryTime = lastRetryTime + Math.min(curRetry * (long)stepSize, maxWait);
        }

        if (now < nextRetryTime)
            return false;

        curRetry++;

        nextRetryTime = -1;
        lastRetryTime = System.currentTimeMillis();

        return true;
    }

    public long getRetryCount() {
        return curRetry;
    }
}
