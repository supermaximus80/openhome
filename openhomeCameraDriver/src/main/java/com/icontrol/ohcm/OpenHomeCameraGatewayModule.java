package com.icontrol.ohcm;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icontrol.openhome.data.Api;
import com.icontrol.openhome.data.CreateMediaTunnel;
import com.icontrol.openhome.data.DeviceInfo;
import com.icontrol.openhome.data.FirmwareDownload;
import com.icontrol.openhome.data.MediaUpload;
import com.icontrol.openhome.data.NetworkInterface;
import com.icontrol.openhome.data.NetworkInterfaceList;
import com.icontrol.openhome.data.NotificationWrapper;
import com.icontrol.openhome.data.PTZData;
import com.icontrol.openhome.data.ResponseStatus;
import com.icontrol.openhome.data.ResponseStatusExt;
import com.icontrol.openhome.data.StringCap;
import com.icontrol.openhome.data.VectorCap;
import com.icontrol.openhome.data.WirelessNetworkStatus;

public class OpenHomeCameraGatewayModule extends OpenHomeCameraGatewayDriver implements OcgmCameraDriver {
	
	//Connection Factory instantiated by caller
	OpenHomeConnectionFactory openHomeConnectionFactory;
	
	//Map of URLs that correspond to openhome camera commands
	Map<String, String> commands;
	
    // user authentication credentials
	private String adminUser = null;
	private String adminPassword = null;
	
    // Base networking URL of device
    protected URL locationURL;
    
    // Connect timeout (ms) for URL connection
    protected int connectTimeout = 10000;
    
    // Read timeout (ms) for URL connection
    protected int readTimeout = 20000;
	
    // Firmware Upgrade Read Timeout in milliseconds 
    protected int firmwareUpgradeReadTimeout = 10000;
    
    // Video Clip  upload Read Timeout in milliseconds 
    protected int clipUploadReadTimeout = 10000;
    
    //SLF4J Logger
    Logger logger;
    
    /*
     * List of Poll Notification Listeners
     */
    private volatile List<NotificationListener> listeners = new ArrayList<NotificationListener>();
    
    /*
     * Thread for event polling.
     */
    private PollingThread pollingThread; 
    
    /*
     * The linger time in seconds for the getSystemPollNotifications command.
     */
    private String linger = "10";
	
	public OpenHomeCameraGatewayModule(String cameraURL, String username, String password, OpenHomeConnectionFactory openHomeConnectionFactory) throws Exception {
		super();
		
		this.openHomeConnectionFactory = openHomeConnectionFactory;
		this.locationURL = new URL(cameraURL);
    	commands = defaultCommandMap;
    	adminUser = username;
    	adminPassword = password;
    	logger = LoggerFactory.getLogger(this.getClass().getName() + ":" + locationURL);
    	
    	/*
    	 * If the device has implemented the Api command, use it to get its command list.
    	 */
    	try {
			ResponseStatus responseStatus = getApi();
			Map<String, String> apiCommands = new HashMap<String, String>();
			for (StringCap commandCap : ((Api) responseStatus.getExtensions().getAny().get(0)).getCommand()) {
				String command = commandCap.getValue().toLowerCase();
				command.replace(Constants.NOTIFYID_REPLACEMENT_STRING, Constants.ID_REPLACEMENT_STRING);
				command.replace(Constants.REGIONID_REPLACEMENT_STRING, Constants.ID_REPLACEMENT_STRING);
				apiCommands.put(command, command);
			}
			commands = apiCommands;
		} catch (Exception e) {
			logger.error(e.getMessage(), e.getCause());
			e.printStackTrace();
		}
    	
	}
	
	/**
	 * This is a convenience method that will extract the bound object from 
	 * the ResponseStatus object. If the ResponseStatus is an error response
	 * or there is no bound object, the ResponseStatus object is returned.
	 * 
	 * @param responseStatus - ResponseStatus
	 * @return - Object bound to ResponseStatus
	 */
	public Object getResponseObject(ResponseStatus responseStatus) {
		
		if (!responseStatus.getStatusString().equals(Constants.OK)) return responseStatus;
		
		try {
			return responseStatus.getExtensions().getAny().get(0);
		} catch (Exception e) {
			return responseStatus;
		}
		
	}

	public URL getLocationURL() {
		return this.locationURL;
	}

    public void setLocationURL(URL cameraURL)
    {
        this.locationURL = cameraURL;
    }
	
	public String getDefaultRootUser() {
		return DEFAULT_ROOT_USER;
	}

	public String getDefaultRootPassword() {
		return DEFAULT_ROOT_PASSWORD;
	}

	public void setAdminUser(String adminUser) {
		this.adminUser = adminUser;
	}
	
	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}
	
	public String getAdminUser() {
		return this.adminUser;
	}

	
	public String getAdminPassword() {
		return this.adminPassword;
	}

	
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	
	public int getConnectTimeout() {
		return this.connectTimeout;
	}

	
	public void setReadTimeout(int readTimeout) {
		this.readTimeout= readTimeout;
	}

	
	public int getReadTimeout() {
		return this.readTimeout;
	}

	public int getFirmwareUpgradeReadTimeout() {
		return firmwareUpgradeReadTimeout;
	}

	public void setFirmwareUpgradeReadTimeout(int firmwareUpgradeReadTimeout) {
		this.firmwareUpgradeReadTimeout = firmwareUpgradeReadTimeout;
	}

	public int getClipUploadReadTimeout() {
		return clipUploadReadTimeout;
	}

	public void setClipUploadReadTimeout(int clipUploadReadTimeout) {
		this.clipUploadReadTimeout = clipUploadReadTimeout;
	}

	public boolean isAlive() throws CameraException {
		
		for (int i = 0; i < 2; i++) {
			ResponseStatus responseStatus = super.systemPing();
			if (responseStatus.getStatusString().equals(Constants.OK)) {
				return true;
			}
		}
		
		return false;
		
	}

	public Integer getSignalStrength() throws CameraException {
		
		ResponseStatus responseStatus = super.getSystemNetworkInterfaces();
		
		try {
			NetworkInterfaceList networkInterfaceList = (NetworkInterfaceList) responseStatus.getExtensions().getAny().get(0);
			for (NetworkInterface networkInterface  : networkInterfaceList.getNetworkInterface()) {
				if (networkInterface.getWireless() != null) {
					return getSignalStrength(networkInterface.getId().getValue());
				}
			}
		} catch (Exception e) {
			logger.error(Constants.MESSAGE_ERROR_ACCESSING_NETWORKINTERFACE_OBJECT + " command: " + responseStatus.getRequestURL() + " : " + e.getMessage(), e.getCause());
			e.printStackTrace();
		}
			
		return 0;
	}
	
	public Integer getSignalStrength(String networkInterfaceId) throws CameraException {
		
		ResponseStatus responseStatus = super.getSystemNetworkInterfacesWirelessStatus(networkInterfaceId);
		
		try {
			WirelessNetworkStatus wirelessNetworkStatus = (WirelessNetworkStatus) responseStatus.getExtensions().getAny().get(0);
			return wirelessNetworkStatus.getRssidB().getValue().intValue();
		} catch (Exception e) {
			throw new CameraException(e.getMessage(), e.getCause());
		}
		
	}

	public String getFirmwareVersion() throws CameraException {

		ResponseStatus responseStatus = super.getSystemDeviceInfo();
		
		try {
			DeviceInfo deviceInfo = (DeviceInfo) responseStatus.getExtensions().getAny().get(0);
			return deviceInfo.getFirmwareVersion().getValue().toString();
		} catch (Exception e) {
			return null;
		}
		
	}
	
	public ResponseStatus upgradeFirmwareFrom(URL fwUpgradeURL) throws CameraException {
		FirmwareDownload firmwareDownload = new FirmwareDownload();
		StringCap url = new StringCap();
		url.setValue(fwUpgradeURL.toString());
		firmwareDownload.setUrl(url);
		upgradeFirmware(firmwareDownload);
		return null;	
	}

	public ResponseStatus upgradeFirmware(FirmwareDownload firmwareDownload) throws CameraException {
		return systemUpdateFirmware(firmwareDownload);
	}
	
	public ResponseStatus reboot() throws CameraException {
		return systemReboot();
	}
	
	public ResponseStatus reset() throws CameraException {
		return systemReset();
	}
	
	
	public ResponseStatus getLogs() throws CameraException {
		return getLogs(null);
	}
	
	public ResponseStatus getLogs(String sinceDateTime) throws CameraException {
		return getSystemLoggingLogData(sinceDateTime);
	}

	public String getSignal() throws CameraException {
		return getSignalStrength().toString();
	}


	public byte[] snapshot() throws CameraException {

		ResponseStatus responseStatus = getSnapshot(null, null, null);
		byte[] errorReturn = {};
		ByteArrayOutputStream response = null;
		
		try {
			response = (ByteArrayOutputStream) responseStatus.getExtensions().getAny().get(0);
		}
		catch(Exception e) {
			return errorReturn;
		}
		
		return response.toByteArray();
		 
	}
	
	public byte[] snapshot(String videoResolutionWidth, String videoResolutionHeight, String fixedQuality)  throws CameraException {
		
		ResponseStatus responseStatus = getSnapshot(videoResolutionWidth, videoResolutionHeight, fixedQuality);
		byte[] errorReturn = {};
		String response = null;
		
		try {
			response = (String) responseStatus.getExtensions().getAny().get(0);
		}
		catch(Exception e) {
			return errorReturn;
		}
		
		return response.getBytes();
	}
	

    public ResponseStatus getSnapshot() throws CameraException {
    	return getSnapshot(null, null, null);
    }
	
	public ResponseStatus getSnapshot(String videoResolutionWidth, String videoResolutionHeight, String fixedQuality) throws CameraException {
//		return getSnapshot(Constants.DEFAULT_CHANNEL_ID, null, null, null);
		return getSnapshot("1", null, null, null);
	}
	
	public ResponseStatus getSnapshot(String id, String videoResolutionWidth, String videoResolutionHeight, String fixedQuality) throws CameraException {
		return streamingChannelsIdPicture(id, videoResolutionWidth, videoResolutionHeight, fixedQuality);
	}
	

	
	public ResponseStatus postSnapshot(MediaUpload mediaUpload) throws CameraException {

		return postSnapshot(mediaUpload, Constants.DEFAULT_CHANNEL_ID);
		
	}

	public ResponseStatus postSnapshot(MediaUpload mediaUpload, String channelId) throws CameraException {

		return streamingChannelsIdPictureUpload(channelId, mediaUpload);
		
	}

	
	public ResponseStatus postVideoClip(MediaUpload mediaUpload) throws CameraException {
		
		return streamingChannelsIdVideoUpload(mediaUpload);
		
	}
	
	public ResponseStatus postVideoClip(MediaUpload mediaUpload, String channelId) throws CameraException {
		
		return streamingChannelsIdVideoUpload(channelId, mediaUpload);
		
	}
	
	
	public ResponseStatus postLiveVideo(CreateMediaTunnel createMediaTunnel)  throws CameraException {
		return streamingChannelMediaTunnelCreate(createMediaTunnel);
	}

	
	public ResponseStatus getPTZConfiguration() throws CameraException {
		return getPtzChannels();
	}
	
	public ResponseStatus getPTZConfiguration(String channelId) throws CameraException {
		return getPtzChannels(channelId);
	}
	
	public ResponseStatus getCurrentPosition(String channelId) throws CameraException {
		return getPtzChannelsStatus(channelId);
	}

	
	public ResponseStatus setHomePosition(String channelId) throws CameraException {
		return setPtzChannelsHomePosition(channelId);
	}

	
	public ResponseStatus setPanTiltZoomRelative(String channelId, String pan, String tilt, String zoom) throws CameraException {
		
		VectorCap panValue = new VectorCap();
		panValue.setValue(Integer.parseInt(pan)); 
		VectorCap tiltValue = new VectorCap();
		tiltValue.setValue(Integer.parseInt(tilt)); 
		VectorCap zoomValue = new VectorCap();
		zoomValue.setValue(Integer.parseInt(zoom)); 
		PTZData ptzData = new PTZData();
		ptzData.setPan(panValue);
		ptzData.setTilt(tiltValue);
		ptzData.setZoom(zoomValue);
		
		return setPtzChannelsRelative(channelId, ptzData);
		
	}

	
	public ResponseStatus setPanTiltZoomAbsolute(String channelId, String pan, String tilt, String zoom) throws CameraException {
		
		VectorCap panValue = new VectorCap();
		panValue.setValue(Integer.parseInt(pan)); 
		VectorCap tiltValue = new VectorCap();
		tiltValue.setValue(Integer.parseInt(tilt)); 
		VectorCap zoomValue = new VectorCap();
		zoomValue.setValue(Integer.parseInt(zoom)); 
		PTZData ptzData = new PTZData();
		ptzData.setPan(panValue);
		ptzData.setTilt(tiltValue);
		ptzData.setZoom(zoomValue);
		
		return setPtzChannelsAbsolute(channelId, ptzData);
		
	}
	
	
	/**
	 * Get the event polling linger time.
	 * 
	 * @return (String) polling linger time in seconds.
	 */
	public String getLinger() {
		return linger;
	}

	/**
	 * Set the event polling linger time.
	 * 
	 * @param linger - (String) polling linger time in seconds.
	 */
	public void setLinger(String linger) {
		this.linger = linger;
	}

	/**
	 * 
	 * @param notificationListener
	 */
	public void addEventListener(NotificationListener notificationListener) {
		
		synchronized(listeners) {
			listeners.add(notificationListener);
			if (pollingThread == null) {
				pollingThread = new PollingThread();
				pollingThread.start();
			}
			
		}
		
	}
	
	/**
	 * Remove a listener from the list. If the list is empty, stop the polling thread.
	 * 
	 * @param notificationListener - NotificationListener
	 */
	public void removeEventListener(NotificationListener notificationListener) {
		
		synchronized(listeners) {
			listeners.remove(notificationListener);
			if (listeners.isEmpty()) {
				pollingThread.stopThread();
				pollingThread = null;
			}
		}
		
	}
	
    /**
     * Build the HTTP request and send it to the device.
     * 
     * @param requestType - (required) HTTP request type (GET, PUT, POST, DELETE)
     * @param command - (required) restful path of service
     * @param parameters - (may be null) Http request parameters as an array ordered as "name","value" pairs.
     * @param parameterObject - (may be null) object to be xml encoded and passed with http request as a parameter (e.g. &ltDeviceInfo&gt).
     * @return - Response
     * @throws CameraException
     */
    public ResponseStatus issueCameraCommand(String requestType, String command, String[] parameters, Object ids, Object parameterObject) throws CameraException {
    	
    	if (requestType == null || command == null) {
    		logger.error(Constants.MESSAGE_REQUIRED_PARAMETER_IS_NULL + " request Type: " + requestType + " command: " + command);
    		throw new InvalidParameterException(Constants.MESSAGE_REQUIRED_PARAMETER_IS_NULL);
    	}
    	
    	/*
    	 * Find the actual path for the command in the map.
    	 */
    	String commandPath = commands.get(command.toLowerCase());
    	
    	if (commandPath == null) {
    		logger.error(Constants.MESSAGE_COMMAND_NOT_SUPPORTED + ": " + command);
    		throw new InvalidParameterException(Constants.MESSAGE_COMMAND_NOT_SUPPORTED);
    	}
    	
    	String url = null;
    	OpenHomeConnection connection = null;
    	DataInputStream rsStream = null;
    	ResponseStatus responseStatus = null;
    	int responseCode = 0;
    	String parameterObjectXml = null;
    	
    	/*
    	 * The parameters could contain an id or array of ids.
    	 */
    	String[] id = null;
    	if (!(ids instanceof String[]) && ids instanceof String) {
    		id = new String[] {(String)ids};
    	} else {
    		id = (String[])ids;
    	}
    	
    	/*
    	 * The command will have a placeholder for an id if one is meant to be in the command path.
    	 */
    	if (commandPath.contains(Constants.ID_REPLACEMENT_STRING) && id == null) {
    		logger.error(Constants.MESSAGE_REQUIRED_PARAMETER_IS_NULL + " id: " + commandPath);
			throw new InvalidParameterException(Constants.MESSAGE_REQUIRED_PARAMETER_IS_NULL);
    	}
    	/*
    	 * Replace placeholders in the commandpath until there are no more.
    	 */
    	if (id != null) {
    		for (int i = 0 ; i < id.length ; i++) {
    			commandPath = commandPath.replace(Constants.ID_REPLACEMENT_STRING, id[i]);
    		}
    	}
    	/*
    	 * It there are still placeholders in the command path it means there weren't
    	 * enough ids in the passed parameters. 
    	 */
    	if (commandPath.contains(Constants.ID_REPLACEMENT_STRING)) {
    		logger.error(Constants.MESSAGE_REQUIRED_PARAMETER_IS_NULL + " id: " + commandPath);
			throw new InvalidParameterException(Constants.MESSAGE_REQUIRED_PARAMETER_IS_NULL);
    	}
    	else {
    		url = locationURL + commandPath;
    	}
    	
    	if (parameterObject != null) {
    		parameterObjectXml = XmlUtilities.marshalObject(parameterObject);
    		logger.info("Parameter xml: " + parameterObjectXml);
    	}
    	
    	logger.info("command path: " + commandPath);
    	
    	connection = openHomeConnectionFactory.getOpenHomeConnection(requestType, 
		    														url, 
		    														parameters, 
		    														parameterObjectXml,
													                adminUser,
													                adminPassword,
		    														connectTimeout, 
		    														readTimeout);
		
    	try {
    		
    		connection.connect();
    		
    		responseCode = connection.getResponseCode();
		
			// read the response		
			if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
				rsStream = new DataInputStream(connection.getInputStream());
			}
			else {
				rsStream = new DataInputStream(connection.getErrorStream());
			}
			
			/*
			 * A ByteArrayOutputStream is the most portable format until
			 * we can decide what this is.
			 */
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int count;
			byte[] data = new byte[16384];

			while ((count = rsStream.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, count);
			}

			buffer.flush();
						
			logger.info("Response Body: " + buffer.toString());
			
			Object responseObject = XmlUtilities.unmarshalObject(buffer.toString());
			
			if (responseObject instanceof ResponseStatus) return (ResponseStatus)responseObject;
			
			/*
			 * The streaming picture request will expect a byte stream, otherwise
			 * a string return is out best guess.
			 */
			if (responseObject == null) {
				if (command.equalsIgnoreCase(API_STREAMING_CHANNELS_ID_PICTURE)) {
					responseObject = buffer.toByteArray();
				} else {
					responseObject = buffer.toString();
				}
				
			}
			
	    	responseStatus = new ResponseStatus();
			ResponseStatusExt responseStatusExt = new ResponseStatusExt();
			responseStatusExt.getAny().add(responseObject);
			responseStatus.setExtensions(responseStatusExt);
			
			responseStatus.setRequestURL(commandPath);
			if (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
				responseStatus.setStatusString(Constants.ERROR);
                responseStatus.setStatusCode(3);
			}
			else {
				responseStatus.setStatusString(Constants.OK);
                responseStatus.setStatusCode(1);
			}
						
			//Remember to tidy up.
			rsStream.close();
			connection.disconnect();

		} catch (Exception e) {
			logger.error(Constants.MESSAGE_CONNECTION_ERROR + " commandPath: " + commandPath + " message: " + e.getMessage());
			throw new CameraException(commandPath, responseCode, null, e.getMessage(), e.getCause());
		} 
    	    	
    	return responseStatus;
    	
    }
    
    /**
     * This inner class implements the thread for polling the device for events.
     * 
     * @author wek
     *
     */
    private class PollingThread extends Thread {
    	
		private volatile boolean polling = true;
		
		public void run() {
						
			try {
				while (polling) {
					System.out.println("linger: " + linger);
					setReadTimeout(new Integer((linger+2)) * 1000);
					ResponseStatus responseStatus = getSystemPollNotifications(linger);
					System.out.println("responseStatus: " + responseStatus.getStatusString());
					NotificationWrapper notificationWrapper = (NotificationWrapper) responseStatus.getExtensions().getAny().get(0);
					for (NotificationListener listener : listeners) {
						listener.postNotification(notificationWrapper);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				pollingThread = null;
			}
			
		}
		
		public void stopThread() {
			polling = false;
		}
    	
    }

}
