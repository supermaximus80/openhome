package com.icontrol.openhomestresstest.job;

import com.icontrol.openhomestresstest.StressTester;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalJob implements Job {
	
	private static final Logger logger = LoggerFactory.getLogger(NormalJob.class);

    private static long jobIndex = 0;
	
	//check the waiting time and execute the actual job to send event.
	public void execute(JobExecutionContext context)
		throws JobExecutionException {

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();

		StressTester[] testers = (StressTester[])dataMap.get("testers");
		int startTestId = (Integer)dataMap.get("startTestId");
		int endTestId = (Integer)dataMap.get("endTestId");
		
		try {
			for (int i = startTestId; i <= endTestId; i++) {
				testers[i].run();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

        jobIndex++;
        if ( (jobIndex % 20) == 0 )
		    logger.debug("JobJobJobJobJobJobJobJobJobJobJobJobvvJobJob Job: " + context.getJobDetail().getKey().getName() + " Total #Jobs ran:"+jobIndex);
	}
}
