package com.icontrol.openhomestresstest.stats;

import com.icontrol.openhomestresstest.StressTester;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class StatsTracker {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(StatsTracker.class);

    static long DEFAULT_REPORT_INTERVAL =  60 * 1000;    // 1 min by default

    private Properties properties = null;
    private StressTester[] testers;
    private long reportInterval = DEFAULT_REPORT_INTERVAL;
    private long lastReportTime;
    private Stats totalStats;
    private Stats recentStats;
    private int numReports;


    public StatsTracker(Properties properties, StressTester[] testers) {
        this.properties = properties;
        this.testers = testers;
        reportInterval = DEFAULT_REPORT_INTERVAL;
        lastReportTime = System.currentTimeMillis() - reportInterval;
        totalStats = new Stats();
        recentStats = new Stats();

        resetStats();
    }

    private void resetStats() {
        totalStats.reset();
        recentStats.reset();
        numReports = 0;
    }

	/*

	 */
	public void run() {
        // check report interval
        try {
            reportInterval = Long.parseLong(properties.getProperty("stats.reportinterval"));
        } catch (Exception ex) {}

        // time to report stats?
        long now = System.currentTimeMillis();
        if ( (now - lastReportTime) < reportInterval ) {
            return;
        }
        lastReportTime = now - 3000;   // allow for clock drift

        // collect stats
        collect_stats();

        // print report
        print_report();

        // reset recent stats
        recentStats.reset();

    }


    private void collect_stats() {
        if (testers == null)
            return;

        try {
            for (int i=0; i<testers.length; i++) {
                testers[i].AddStats(recentStats);
                testers[i].resetStats();
            }

            // add to total
            totalStats.AddStats(recentStats);
        } catch (Exception ex) {
            logger.error("collect_stats caught "+ex, ex);
        }
    }

    private void print_report() {

        try {
            if (numReports++ == 0)
                recentStats.print_header();

            recentStats.print_report();
        } catch (Exception ex) {
            logger.error("print_report caught "+ex, ex);
        }

    }


 }
