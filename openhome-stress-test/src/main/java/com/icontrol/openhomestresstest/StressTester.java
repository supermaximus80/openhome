package com.icontrol.openhomestresstest;

import com.icontrol.openhomesimulator.camera.CameraSimulator;
import com.icontrol.openhomesimulator.camera.xmppclient.OpenHomeRequestIQ;
import com.icontrol.openhomesimulator.camera.xmppclient.OpenHomeResponseIQ;
import com.icontrol.openhomesimulator.camera.xmppclient.XmppClientIQListener;
import com.icontrol.openhomestresstest.event.EventHandler;
import com.icontrol.openhomestresstest.event.MotionEventHandler;
import com.icontrol.openhomestresstest.stats.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class StressTester implements XmppClientIQListener {

    public static String MOTION_ALERT_PACKETID_SUFFIX = ".M.";

    private static final Logger logger = LoggerFactory.getLogger(StressTester.class);

	private String xmppHost = null;
	private int xmppPort = 5222;

    private String serialNo = null;
    private String siteId = null;
    private String sharedSecret = null;

    private CameraSimulator cameraSimulator = null;

    private boolean isRestarting = false;
    private boolean isConnecting = false;
    long restartBeginTime = 0;

	private boolean isConnectedToServer = false;

	private boolean isBlocked = false;
	private boolean isRunning = false;

	private boolean isInTest = true;

    private Properties properties = null;
    private EventHandler[] eventHandlers = null;
    private MotionEventHandler motionEventHandler = null;

    public static int MOTION_ALERT_HANDLER_INDEX = 0;
    public static int TOTAL_NUM_HANDLERS = 1;

    private RetryTracker retryTracker = null;

    private Stats stats;

    public StressTester() {
        stats = new Stats();
    }

    @Override
    public boolean processRequestIQ(OpenHomeRequestIQ iq) {
        // do nothing, process request from Gateway in camera simulator
        return false;
    }

    @Override
    public boolean processResponseIQ(OpenHomeResponseIQ iq) {
        try {
            String packetId = iq.getPacketID();

            if(packetId != null) {
                if (packetId.indexOf(MOTION_ALERT_PACKETID_SUFFIX) >=0) {
                    return eventHandlers[MOTION_ALERT_HANDLER_INDEX].processResponseIQ(iq);
                } else {
                    logger.error("processResponseIQ unknown packetId type: "+packetId+" for to="+iq.getTo());
                }
            }
        } catch (Exception ex) {
            logger.error("processResponseIQ caught "+ex, ex);
        }

        return false;
    }

    @Override
    public void disconnected() {
        logger.debug("Xmpp disconnected for sieId:"+siteId+" serialNo:"+serialNo);
        if (retryTracker != null)
            retryTracker.reset();
        isConnectedToServer = false;
    }

    public CameraSimulator getCameraSimulator() {
		return cameraSimulator;
	}

	public void init(Properties properties) throws Exception {
		this.xmppHost = properties.getProperty("xmpp.host");
        this.xmppPort = 5222;
        try {
		    this.xmppPort = Integer.parseInt(properties.getProperty("xmpp.port"));
        } catch (Exception ex) {}

        // create event handlers
        if (eventHandlers == null) {
            eventHandlers = new EventHandler[TOTAL_NUM_HANDLERS];

            motionEventHandler = new MotionEventHandler();
            eventHandlers[MOTION_ALERT_HANDLER_INDEX] = motionEventHandler;
        }

        // init event handlers
        for (int i = 0; i < eventHandlers.length; i++) {
            eventHandlers[i].init(siteId, serialNo, this, properties);
        }

        // save properties file
        this.properties = properties;
	}

	/*
	 * Check if the XMPP connection is good or not and recreate a new one if it is not connected.
	 * Run this method, from Normal cron job class, and run all event handler
	 */
	public void run() {

        // init params if first time
        if (retryTracker == null)
            retryTracker = new RetryTracker(properties);

        // check xmpp connection status
        if (cameraSimulator != null)
            this.isConnectedToServer = cameraSimulator.isXMPPconnected();

        if (isConnecting) {
            logger.warn("Early return from run while in connecting state for SiteId:"+getSiteId()+" serialNo:"+serialNo);
            return;
        }

		//checking if the tester in restarting status, yes, check how long has been, if it is after 1 min, restart again
		//otherwise still waiting, and do nothing in this round.
		if (this.isRestarting || !this.isConnectedToServer)
		{
			if ( retryTracker.timeForNextRetry() )
			{
				try
				{
					this.restartTester();
				}
				catch (Exception e)
				{
					logger.error("Exception when creating connection to XMPP server:", e);
					return;
				}
			}
			else
			{
				return;
			}
		}

		//blocked when update firmware or widget
	    if (isBlocked || isRunning) {
	    	logger.info("SiteId : " + getSiteId() + " : isBlocked -> " + isBlocked +
	    			" : isRunning --> " + this.isRunning + ", so don't run any event handlers");
	    	return;
	    }


		if (!isBlocked) {
			this.isRunning = true;

			logger.trace("SiteId : " + getSiteId() + " is in running mode");

			try {
				for (int i = 0; i < eventHandlers.length; i++) {
					eventHandlers[i].run(this.cameraSimulator);

                    stats.numMotionEvents += eventHandlers[i].getEventsSent();
                    stats.totMotionTime += eventHandlers[i].getSumTimeOfEvents();
                    eventHandlers[i].resetEventTime();
				}
				this.isRunning = false;

			} catch (Exception e)
			{
				logger.error("Exception when run all event handlers:",e);

				if (e.getMessage() != null && e.getMessage().indexOf("Not connected to server") >=0)
				{
					//lost connection.
					try {
						this.restartTester();
						return;
					}
					catch (Exception ee)
					{
						logger.error("Exception when creating connection to XMPP server:", e);
						return;
					}
				}
			}

			logger.trace("SiteId : " + getSiteId() + " finished!");

			this.isRunning = false;
		}
	}


	public boolean createXMPPConnectionAndLogin()
		throws Exception {
		logger.info("SiteId : " + getSiteId() + " serialNo:" + getSerialNo() + ": createXMPPConnectionAndLogin to XmppHost:"+xmppHost+" port:"+xmppPort);

		try
		{
			logger.info("before connect to server of siteId:" + getSiteId());

            if (cameraSimulator == null) {
                cameraSimulator = new CameraSimulator(serialNo, siteId, sharedSecret, xmppHost, xmppPort, null);
                cameraSimulator.setXmppClientIQListener(this);
            }

            long startTime = System.currentTimeMillis();
            isConnecting = true;
            int responseCode = -1;
            try {
                responseCode = cameraSimulator.xmppConnect();
            } catch (Exception ex) {
                logger.info("connect to XMPP Server failed for siteId:" + getSiteId() + " due to "+ex, ex);
            }
            isConnecting = false;
            if (responseCode != 200) {
                stats.numXMPPconnectFailures++;
                logger.info("connect to XMPP Server failed for siteId:" + getSiteId() +" code:"+responseCode);
                return false;
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("connect to XMPP Server successful for siteId:" + getSiteId()+ " connectTime:"+duration);
            stats.numXMPPconnects++;
            stats.totXMPPconnectTime += duration;

            // consider moving this to after receiving initial event
            isRestarting = false;
            isConnectedToServer = true;
            if (retryTracker != null)
                retryTracker.reset();
			return true;
		}
		catch (Exception e)
		{
			logger.error("Cannot get TCP/IP connection for siteId:" + getSiteId() + " :", e);
			Thread.sleep(10000);
		}

		return false;
	}

	public void dropXMPPConnection() {
		if (this.cameraSimulator != null) {
			this.cameraSimulator.xmppDisconnect();
		}
	}

	public boolean  reconnectXMPPConnection() throws Exception {
		return createXMPPConnectionAndLogin();
	}

	public void restartTester()
		throws Exception {

		this.isRestarting = true;

		this.restartBeginTime = System.currentTimeMillis();

		//clean up the status.
		this.isBlocked = false;
		this.isConnectedToServer = false;
		this.isRunning = false;


		//stop all event handlers
		for (int i = 0; i < eventHandlers.length; i++) {
			eventHandlers[i].stop(true);
		}

		//drop/disconnect the connection
		dropXMPPConnection();

		// Reconnect XMPP connection
		if (reconnectXMPPConnection())
		{
			initializeHandlersLostConnection(this.properties);
		}
	}


	private void initializeHandlersLostConnection(Properties properties) {
		logger.info("SiteId : " + getSiteId() + " : initializeHandlers");

        for (int i = 0; i < eventHandlers.length; i++) {
            eventHandlers[i].stop(false);
            eventHandlers[i].init(serialNo, siteId, this, properties);
        }

	}

	public void setBlockFlag(boolean blocked) {
		this.isBlocked = blocked;
	}

	public boolean getBlockFlag() {
		return this.isBlocked;
	}

	public String getXmppHost() {
		return xmppHost;
	}

	public boolean isRestarting() {
		return isRestarting;
	}

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public void AddStats(Stats sum) {

        if (!isConnectedToServer)
            logger.info("Camera serialNo:"+serialNo+" NOT connected to XMPP server. retryCount:"+(retryTracker==null?0:retryTracker.getRetryCount()));

        stats.numActiveXMPPconnections = isConnectedToServer ? 1 : 0;

        stats.AddStats(sum);
    }

    public void resetStats() {
        stats.reset();
    }

}
