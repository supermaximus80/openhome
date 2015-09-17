package com.icontrol.openhomestresstest;

import com.icontrol.openhomestresstest.job.LoadPropertyFileJob;
import com.icontrol.openhomestresstest.job.NormalJob;
import com.icontrol.openhomestresstest.job.PeakJob;
import com.icontrol.openhomestresstest.stats.StatsTracker;
import org.jivesoftware.smack.SmackConfiguration;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class StressTestSchedular {

    private static final Logger logger = LoggerFactory.getLogger(StressTestSchedular.class);

    private static Scheduler scheduler = null;
    private static StressTester testers[] = null;
    private static StatsTracker statsTracker = null;

    private void setNormalCronSchedular(String jobId, int startTestId, int endTestId, int delay) throws Exception {
        // Define job instance
        JobDetailImpl job = new JobDetailImpl();
        job.setName("job " + jobId);
        job.setGroup("group");
        job.setJobClass(NormalJob.class);

        //set a new tester
        job.getJobDataMap().put("testers", testers);
        job.getJobDataMap().put("startTestId", new Integer(startTestId));
        job.getJobDataMap().put("endTestId", new Integer(endTestId));

        // Define a Trigger that will fire "now"
        CronTriggerImpl trigger = new CronTriggerImpl();
        trigger.setName("cronTrigger" + jobId);
        trigger.setGroup("cronGroup");
        String cronExpress = Integer.parseInt(jobId) % 60 + "/5" + " 0-59 * * * ?";
        trigger.setCronExpression(cronExpress);

        // Schedule the job with the trigger
        scheduler.scheduleJob(job, trigger);
    }


    private void setPeakSchedular(Properties properties) throws Exception {

        String startCronExpress = null;
        String endCronExpress = null;

        startCronExpress = properties.getProperty("peak.start.time");
        endCronExpress = properties.getProperty("peak.end.time");

        // Define job instance
        JobDetailImpl job = new JobDetailImpl();
        job.setName("peakJobStart");
        job.setGroup("peakGroup");
        job.setJobClass(PeakJob.class);

        //set a new tester
        job.getJobDataMap().put("isStart", Boolean.TRUE);
        job.getJobDataMap().put("testers", testers);
        job.getJobDataMap().put("properties", properties);

        CronTriggerImpl trigger = new CronTriggerImpl();
        trigger.setName("peakTriggerStart");
        trigger.setGroup("peakGroup");
        trigger.setCronExpression(startCronExpress);

        // Schedule the job with the trigger
        scheduler.scheduleJob(job, trigger);


        // Define job instance
        JobDetailImpl job2 = new JobDetailImpl();
        job2.setName("peakJobEnd");
        job2.setGroup("peakGroup");
        job2.setJobClass(PeakJob.class);

        //set a new tester
        job2.getJobDataMap().put("isStart", Boolean.FALSE);
        job2.getJobDataMap().put("testers", testers);
        job2.getJobDataMap().put("properties", properties);

        CronTriggerImpl trigger2 = new CronTriggerImpl();
        trigger2.setName("peakTriggerEnd");
        trigger2.setGroup("peakGroup");
        trigger2.setCronExpression(endCronExpress);

        // Schedule the job with the trigger
        scheduler.scheduleJob(job2, trigger2);

    }

    /*
    private void setEventDailyLimitSchedular(Properties properties) throws Exception {

        // Define job instance
        JobDetailImpl job = new JobDetailImpl("eventDailyLimitJob", "dailyLimitGroup", ResetEventDailyLimitJob.class);
        job.setName("eventDailyLimitJob");
        job.setGroup("dailyLimitGroup");
        job.setJobClass(ResetEventDailyLimitJob.class);

        //set a new tester
        job.getJobDataMap().put("testers", testers);

        // Define a Trigger that will fire "now"
        CronTriggerImpl trigger = new CronTriggerImpl();
        trigger.setName("dailyLimitTrigger");
        trigger.setGroup("dailyLimitGroup");
        String cronExpress = "0 0 1 * * ?";
        trigger.setCronExpression(cronExpress);

        // Schedule the job with the trigger
        scheduler.scheduleJob(job, trigger);
    }
    */

    private void setPropertyFileChangeSchedular(Properties properties, String propPath, StatsTracker statsTracker) throws Exception {

        // Define job instance
        JobDetailImpl job = new JobDetailImpl();
        job.setName("jobCheckPropFile");
        job.setGroup("groupCheckPropFile");
        job.setJobClass(LoadPropertyFileJob.class);

        //set a new tester
        job.getJobDataMap().put("testers", testers);
        job.getJobDataMap().put("oldProperty", properties);
        job.getJobDataMap().put("lastModifyTime", new Long(System.currentTimeMillis()));
        job.getJobDataMap().put("path", propPath);
        job.getJobDataMap().put("statsTracker", statsTracker);

        // Define a Trigger that will fire "now"

        CronTriggerImpl trigger = new CronTriggerImpl();
        trigger.setName("triggerCheckPropFile");
        trigger.setGroup("groupCheckPropFile");
        String cronExpress = "0" + " * * * * ?";
        trigger.setCronExpression(cronExpress);

        // Schedule the job with the trigger
        scheduler.scheduleJob(job, trigger);
    }

    private static String genIDs(String prefix, String indexStr, int idLength) throws IOException {
        int fillLen =  idLength - prefix.length() - indexStr.length();
        if (fillLen < 0)
            throw new IOException("Invalid genIDs len "+prefix+" "+indexStr+" "+idLength);

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        for (int i=0; i < fillLen; i++)
            sb.append("0");
        sb.append(indexStr);
        return sb.toString();
    }


    public static void main(String[] args) {

        Properties properties = null;

        try {
            SchedulerFactory sf = new StdSchedulerFactory();

            scheduler = sf.getScheduler();

            properties = new Properties();

            String propPath = args[0];
            if (propPath==null)
                propPath = "config/stressTest.properties";

            InputStream inStream = new FileInputStream(propPath);
            properties.load(inStream);
            inStream.close();
            StressTestSchedular stressTestSchedular = new StressTestSchedular();

            int numberOfTesterPerJob = Integer.parseInt(properties.getProperty("tester.per.job"));

            int numCameras = Integer.parseInt(properties.getProperty("test.numcameras"));
            String siteIdPrefix = properties.getProperty("siteid.prefix");
            int siteIdStart = Integer.parseInt(properties.getProperty("siteid.start"));
            String cameraSerialPrefix = properties.getProperty("camera.serial.prefix");
            if (cameraSerialPrefix == null || cameraSerialPrefix.length() == 0)
                cameraSerialPrefix = siteIdPrefix;
            int cameraSerialStart = siteIdStart;
            try {
                cameraSerialStart = Integer.parseInt(properties.getProperty("camera.serial.start"));
            } catch (Exception ex) {}
            String cameraSharedSecretRoot = properties.getProperty("camera.sharedsecret.prefix", "test");

            // set client packet replay timeout config
            int severReplayTimeout = 15000;
            try {
                severReplayTimeout = Integer.parseInt(properties.getProperty("serverpacketreply.timeout"));
            } catch (Exception ex) {}
            SmackConfiguration.setPacketReplyTimeout(severReplayTimeout);

            logger.info("Start STRESS TEST CLIENTS. siteIdPrefix:" + siteIdPrefix + " siteIdStart:"+siteIdStart+" cameraSerialPrefix:"+cameraSerialPrefix+" cameraSerialStart:"+cameraSerialStart+" cameraSharedSecretRoot:"+cameraSharedSecretRoot);
            logger.info("severReplayTimeout = "+severReplayTimeout);

            testers = new StressTester[numCameras];
            int failed = 0;
            String siteId = null;
            String serialNo = null;
            String sharedSecret = null;
            for (int i = 0; i < numCameras; i++) {
                try {
                    testers[i] = new StressTester();

                    testers[i].init(properties);

                    // set siteId/serialNo/sharedSecret
                    siteId = genIDs(siteIdPrefix, Integer.toString(siteIdStart+i), 12);
                    serialNo = genIDs(cameraSerialPrefix, Integer.toString(cameraSerialStart+i), 12);
                    sharedSecret = cameraSharedSecretRoot + serialNo;
                    testers[i].setSiteId(siteId);
                    testers[i].setSerialNo(serialNo);
                    testers[i].setSharedSecret(sharedSecret);

                    logger.info("tester siteId:" + siteId + " serialNo:"+serialNo+" initialized!");
                } catch (Exception e) {
                    failed++;
                    logger.error(e.getMessage());
                    if(logger.isDebugEnabled()) {
                        e.printStackTrace();
                    }
                }
            }

            // init stats tracker
            statsTracker = new StatsTracker(properties, testers);

            logger.info("TOTAL " + numCameras + " STRESS CLIENTS ARE INITIALIZED!");
            logger.info("Normal task schedular for users:" + numCameras);
            int i = 0;

            for (; i < numCameras/numberOfTesterPerJob; i++) {
                stressTestSchedular.setNormalCronSchedular("" + i, i * numberOfTesterPerJob, i * numberOfTesterPerJob + numberOfTesterPerJob - 1, i);

                logger.info("Normal task schedular for job: " + i + " is setup!");
            }

            logger.info("TOTAL " + i + " NORMAL CRON JOBS ARE INITIALIZED!");
            if (numCameras%numberOfTesterPerJob > 0) {

                stressTestSchedular.setNormalCronSchedular("" + i, i * numberOfTesterPerJob, i * numberOfTesterPerJob + numCameras%numberOfTesterPerJob - 1,1);
            }

            /*
            logger.info("Peak schedular");
            stressTestSchedular.setPeakSchedular(properties);
            */

            logger.info("Property file change schedular");
            stressTestSchedular.setPropertyFileChangeSchedular(properties, propPath, statsTracker);

            // stressTestSchedular.setEventDailyLimitSchedular(properties);

            logger.info("Start schedular");
            scheduler.start();

        } catch (Exception ee) {
            logger.error(ee.getMessage());
            ee.printStackTrace();
        }
    }
}
