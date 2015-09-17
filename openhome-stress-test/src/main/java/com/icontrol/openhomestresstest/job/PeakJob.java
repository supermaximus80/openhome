package com.icontrol.openhomestresstest.job;

import com.icontrol.openhomestresstest.StressTester;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PeakJob implements Job {
	
    private static final Logger logger = LoggerFactory.getLogger(PeakJob.class);
	
	//check the waiting time and execute the actual job to send event.
	public void execute(JobExecutionContext context)
		throws JobExecutionException {

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();

		StressTester[] testers = (StressTester[])dataMap.get("testers");
		
		Boolean isStart = (Boolean)dataMap.get("isStart");
		
		Properties properties = (Properties)dataMap.get("properties");
		int normalWaiting = new Integer(properties.getProperty("wait.time.normal"));
		int peakWaiting = new Integer(properties.getProperty("wait.time.peak"));
		int alarmWaiting = new Integer(properties.getProperty("alarm.interval"));
		
        /* TODO
		for (int i = 0; i < testers.length; i++) {
			if (isStart) {
				testers[i].setMaxWaitingPeriod(peakWaiting, alarmWaiting);
			} else {
				testers[i].setMaxWaitingPeriod(normalWaiting, alarmWaiting);
			}
		}
		*/
		
		logger.info("$$$$$$$$$$$$$$$$$$$$$$$$The peak time has been set up$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
	}
}
