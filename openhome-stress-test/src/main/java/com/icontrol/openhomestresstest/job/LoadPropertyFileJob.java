package com.icontrol.openhomestresstest.job;

import com.icontrol.openhomestresstest.StressTester;
import com.icontrol.openhomestresstest.stats.StatsTracker;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class LoadPropertyFileJob implements Job {
	
	private static final Logger logger = LoggerFactory.getLogger(LoadPropertyFileJob.class);

	//check the waiting time and execute the actual job to send event.
	public void execute(JobExecutionContext context)
		throws JobExecutionException {

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();

		StressTester[] testers = (StressTester[])dataMap.get("testers");
		Properties oldProp = (Properties)dataMap.get("oldProperty");
		
		Long lastModifyTime = (Long)oldProp.get("lastModifyTime");
		
		if (lastModifyTime == null) {
			lastModifyTime = (Long)dataMap.get("lastModifyTime");
		}

        StatsTracker statsTracker = (StatsTracker) dataMap.get("statsTracker");

		String propertyFilePath = (String)dataMap.get("path");
		File propFile = new File(propertyFilePath);
		long modifyTime = propFile.lastModified();
		
		try {			
			if (modifyTime > lastModifyTime) {

				InputStream inStream = new FileInputStream(propertyFilePath);		
	
				Properties properties = new Properties();
				properties.load(inStream);
				inStream.close();	


				oldProp.setProperty("wait.time.normal", properties.getProperty("wait.time.normal"));
				oldProp.setProperty("wait.time.peak", properties.getProperty("wait.time.peak"));

				//Integer normalWaitingOfEvent = Integer.parseInt(properties.getProperty("wait.time.normal"));

				oldProp.put("lastModifyTime", new Long(modifyTime));
				
				logger.info("########################The property has been changed.############################");
			} 
			
			logger.info("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJ Job: " + context.getJobDetail().getKey().getName());
		
		} catch (Exception e) {
			throw new JobExecutionException(e);
		}

        // stats
        try {
            if (statsTracker != null)
                statsTracker.run();
        } catch (Exception ex) {
            logger.error("caught "+ex, ex);
            throw new JobExecutionException(ex);
        }
	}
}
