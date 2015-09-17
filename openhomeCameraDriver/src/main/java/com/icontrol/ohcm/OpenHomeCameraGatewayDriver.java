package com.icontrol.ohcm;

import java.util.ArrayList;
import java.util.List;

import com.icontrol.openhome.data.Account;
import com.icontrol.openhome.data.AudioChannel;
import com.icontrol.openhome.data.AuthorizationInfo;
import com.icontrol.openhome.data.ConfigFile;
import com.icontrol.openhome.data.ConfigTimers;
import com.icontrol.openhome.data.CreateMediaTunnel;
import com.icontrol.openhome.data.DeviceInfo;
import com.icontrol.openhome.data.Discovery;
import com.icontrol.openhome.data.EventNotificationMethods;
import com.icontrol.openhome.data.EventTrigger;
import com.icontrol.openhome.data.EventTriggerList;
import com.icontrol.openhome.data.EventTriggerNotification;
import com.icontrol.openhome.data.EventTriggerNotificationList;
import com.icontrol.openhome.data.FirmwareDownload;
import com.icontrol.openhome.data.HistoryConfiguration;
import com.icontrol.openhome.data.HostNotification;
import com.icontrol.openhome.data.HostNotificationList;
import com.icontrol.openhome.data.HostServer;
import com.icontrol.openhome.data.IEEE8021X;
import com.icontrol.openhome.data.IPAddress;
import com.icontrol.openhome.data.LoggingConfig;
import com.icontrol.openhome.data.MediaUpload;
import com.icontrol.openhome.data.MotionDetection;
import com.icontrol.openhome.data.MotionDetectionRegion;
import com.icontrol.openhome.data.MotionDetectionRegionList;
import com.icontrol.openhome.data.NTPServer;
import com.icontrol.openhome.data.NTPServerList;
import com.icontrol.openhome.data.NetworkInterface;
import com.icontrol.openhome.data.PTZData;
import com.icontrol.openhome.data.ResponseStatus;
import com.icontrol.openhome.data.SoundDetection;
import com.icontrol.openhome.data.StreamingChannel;
import com.icontrol.openhome.data.TemperatureDetection;
import com.icontrol.openhome.data.UserList;
import com.icontrol.openhome.data.VideoInputChannel;
import com.icontrol.openhome.data.Wireless;

/**
 * Service class that implements the several Open Home camera command
 * access methods.
 * 
 * @author wek
 * @version 1.0
 */
public abstract class OpenHomeCameraGatewayDriver implements OpenHomeCameraDriver {
    
    public OpenHomeCameraGatewayDriver() {
    	
    }
    
	/**
     * Get supported commands.
     * <p>
     * Command: /OpenHome/Api
     * 
     * @return ResponseStatus (Api)
     * @throws CameraException 
     */
    public ResponseStatus getApi() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_API, null, null, null);
    }

	/**
     * Reboot device.
     * <p>
     * Command: /OpenHome/System/reboot
     * 
     * @return ResponseStatus
     * @throws CameraException 
     */
    public ResponseStatus systemReboot() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_SYSTEM_REBOOT, null, null, null);
    }
    
	/**
     * Reset device with default mode.
     * <p>
     * Command: /OpenHome/System/factoryReset
     * 
     * @return ResponseStatus
     * @throws CameraException 
     */
    public ResponseStatus systemReset() throws CameraException {
    	return systemReset(null);
    }
    
	/**
     * Reset device with the specified mode.
     * <p>
     * Command: /OpenHome/System/factoryReset
     * 
     * @param mode - "full" or "basic"
     * @return ResponseStatus
     * @throws CameraException 
     */
    public ResponseStatus systemReset(String mode) throws CameraException {
    	
    	if (mode != null) {
    		String[] parameters = {API_SYSTEM_FACTORYRESET_PARAMETER_MODE, mode};
        	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_SYSTEM_FACTORYRESET, parameters, null, null);
    	}
    	
    	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_SYSTEM_FACTORYRESET, null, null, null);
    	
    }
    
    /**
     * Get system logging configuration.
     * <p>
     * Command: /OpenHome/System/logging
     * 
     * @return ResponseStatus (LoggingConfig)
     * @throws CameraException
     */
    public ResponseStatus getSystemLoggingConfig() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_LOGGING, null, null, null);
    }
    
    /**
     * Set system logging configuration.
     * <p>
     * Command: /OpenHome/System/logging
     * 
     * @param loggingConfig - LoggingConfig
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemLoggingConfig(LoggingConfig loggingConfig) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_LOGGING, null, null, loggingConfig);
    }
    
    /**
     * Get the system log entries since the specified date/time.
     * <p>
     * Command: /OpenHome/System/logging/logData
     * 
     * @param queries - String date/time in UTC format.
     * @return ResponseStatus (String logdata)
     * @throws CameraException
     */    
    public ResponseStatus getSystemLoggingLogData(String since) throws CameraException {

    	if (since != null) {
    		String[] parameters = {API_SYSTEM_LOGGING_LOGDATA_PARAMETER_SINCE, since};
        	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_LOGGING_LOGDATA, parameters, null, null);
    	}
    	
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_LOGGING_LOGDATA, null, null, null);
    }
    

    /**
     * Poll for system notifications for the specified length of time.
     * <p>
     * Command: /OpenHome/System/Poll/notifications
     * 
     * @param linger - time to wait in seconds before responding to a request (if no notification is available).
     *                 If a notification is available or becomes available during waiting, a response is sent as
     *                 soon as possible without waiting for the linger expiration.
     * @return - ResponseStatus (NotificationWrapper)
     * @throws CameraException
     */
    public ResponseStatus getSystemPollNotifications(String linger) throws CameraException {
    	
    	if (linger != null) {
    		String[] parameters = {API_SYSTEM_POLL_NOTIFICATIONS_PARAMETER_LINGER, linger};
    		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_POLL_NOTIFICATIONS, parameters, null, null);
    	}
    	
		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_POLL_NOTIFICATIONS, null, null, null);
	}
    
    /**
     * Get DeviceInfo object from the camera.
     * <p>
     * Command: /OpenHome/System/deviceInfo
     * 
     * @return ResponseStatus (DeviceInfo)
     * @throws CameraException
     */
    public ResponseStatus getSystemDeviceInfo() throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_DEVICEINFO, null, null, null);
    }
    
    /**
     * Set DeviceInfo in the camera from object.
     * <p>
     * Command: /OpenHome/System/deviceInfo
     * 
     * @param deviceInfo - DeviceInfo
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemDeviceInfo(DeviceInfo deviceInfo) throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_DEVICEINFO, null, null, deviceInfo);
    }
    
    /**
     * Get host server configuration.
     * <p>
     * Command: /OpenHome/System/Host/server
     * 
     * @return ResponseStatus (HostServer)
     * @throws CameraException
     */
    public ResponseStatus getSystemHostServer() throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_HOST_SERVER, null, null, null);
    }
    
    /**
     * Set host server configuration.
     * <p>
     * Command: /OpenHome/System/Host/server
     * 
     * @param httpServer
     * @return
     * @throws CameraException
     */
    public ResponseStatus setSystemHostServer(HostServer hostServer) throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_HOST_SERVER, null, null, hostServer);
    }
    
    /**
     * This is a convenience method for getSystemHistory(String sinceCommand, String sinceNotification) with no arguments.
     * 
     * @return ResponseStatus (HistoryList)
     * @throws CameraException
     */
    public ResponseStatus getSystemHistory() throws CameraException {
		return getSystemHistory(null, null);
    }
    
    /**
     * Get history of Commands and response status.
     * <p>
     * Command: /OpenHome/System/history
     * <p>
     * sinceCommand and sinceNotification  are UTC in milliseconds, where  UTC in milliseconds
     * is the time in milliseconds since midnight, January 1, 1970 UTC.  If sinceCommand and/or
     * sinceNotification query is used, device should return history entries for Commands or 
     * Notification since <UTC in milliseconds>, respectively. If sinceCommand is 0 or missing,
     * all Commands in the device history buffer should be returned. If sinceNotification is 0 
     * or missing, all Notifications in the device history buffer should be returned.
     * 
     * @param sinceCommand - UTC in milliseconds.
     * @param sinceNotification - UTC in milliseconds.
     * @return ResponseStatus (HistoryList)
     * @throws CameraException
     */
    public ResponseStatus getSystemHistory(String sinceCommand, String sinceNotification) throws CameraException {
    	
    	if (sinceCommand == null && sinceNotification == null) {
    		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_HISTORY, null, null, null);
    	}
    	
    	List<String> parameters = new ArrayList<String>();
    	    	
    	if (sinceCommand != null) {
    		parameters.add(API_SYSTEM_HISTORY_PARAMETER_SINCECOMMAND);
    		parameters.add(sinceCommand);
    	}
    	
    	if (sinceNotification != null) {
    		parameters.add(API_SYSTEM_HISTORY_PARAMETER_SINCENOTIFICATION);
    		parameters.add(sinceNotification);
    	}
    	
		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_HISTORY, (String[]) parameters.toArray(), null, null);
    	
    }
    
    /**
     * Gets the system History configuration.
     * <p>
     * Command: /OpenHome/System/history/configuration
     * 
     * @return ResponseStatus (HistoryConfiguration)
     * @throws CameraException
     */
    public ResponseStatus getSystemHistoryConfiguration() throws CameraException { 
		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_HISTORY_CONFIGURATION, null, null, null);
    }
    
    /**
     * Sets the system History configuration.
     * <p>
     * Command: /OpenHome/System/history/configuration
     * 
     * @param historyConfiguration - HistoryConfiguration
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemHistoryConfiguration(HistoryConfiguration historyConfiguration) throws CameraException { 
		return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_HISTORY_CONFIGURATION, null, null, historyConfiguration);
    }
        
    /**
     * Gets the system time.
     * <p>
     * Command: /OpenHome/System/time
     * 
     * @return ResponseStatus (Time)
     * @throws CameraException
     */
    public ResponseStatus getSystemTime() throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_TIME, null, null, null);
    }
    
    /**
     * Gets the system time zone.
     * <p>
     * Command: /OpenHome/System/time/timeZone
     * 
     * @return ResponseStatus (Time zone as UTC format string)
     * @throws CameraException
     */
    public ResponseStatus getSystemTimeZone() throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_TIME_TIMEZONE, null, null, null);
    }
    
    /**
     * Get the NTP server configurations for the device.
     * <p>
     * Command: /OpenHome/System/time/ntpServers
     * 
     * @return - ResponseStatus (NTPServerList)
     * @throws CameraException
     */
    public ResponseStatus getSystemTimeNtpServers() throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_TIME_NTPSERVERS, null, null, null);
    }
    
    /**
     * Update the NTP server configurations for the device.
     * <p>
     * Command: /OpenHome/System/time/ntpServers
     * 
     * @param ntpServerList - NTPServerList
     * @return - ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemTimeNtpServers(NTPServerList ntpServerList) throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_TIME_NTPSERVERS, null, null, ntpServerList);
    }
    
    /**
     * Get the NTP server configuration for the specified id.
     * <p>
     * Command: /OpenHome/System/time/ntpServers/[uid]
     * 
     * @param ntpServerId - Id of the NTPServer.
     * @return - ResponseStatus (NTPServer)
     * @throws CameraException
     */
    public ResponseStatus getSystemTimeNtpServers(String ntpServerId) throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_TIME_NTPSERVERS_ID, null, ntpServerId, null);
    }
    
    /**
     * Set the NTP server configuration for the specified id.
     * <p>
     * Command: /OpenHome/System/time/ntpServers/[uid]
     * 
     * @param ntpServerId - Id of the NTPServer.
     * @param ntpServer - NTPServer
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemTimeNtpServers(String ntpServerId, NTPServer ntpServer) throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_TIME_NTPSERVERS_ID, null, ntpServerId, ntpServer);
    }
    
    /**
     * Delete the NTP server configuration for the specified id.
     * <p>
     * Command: /OpenHome/System/time/ntpServers/[uid]
     * 
     * @param ntpServerId - Id of the NTPServer.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus deleteSystemTimeNtpServers(String ntpServerId) throws CameraException {
		return issueCameraCommand(Constants.HTTP_REQUEST_DELETE, API_SYSTEM_TIME_NTPSERVERS_ID, null, ntpServerId, null);
    }
    
    /**
     * Test that the device is operable.
     * <p>
     * Command: /OpenHome/System/Ping
     * 
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus systemPing() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_PING, null, null, null);		
    }
    
    /**
     * Update the device firmware from the URL specified in the FirmwareDownload object.
     * <p>
     * Command: /OpenHome/System/updateFirmware
     * 
     * @param firmwareDownload - FirmwareDownload
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus systemUpdateFirmware(FirmwareDownload firmwareDownload) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_SYSTEM_UPDATEFIRMWARE, null, null, firmwareDownload);		
    }
    
    /**
     * Returns the status of a firmware update.
     * <p>
     * Command: /OpenHome/System/updateFirmware/status
     * 
     * @return ResponseStatus (UpdateFirmwareStatus)
     * @throws CameraException
     */
    public ResponseStatus getSystemUpdateFirmwareStatus() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_UPDATEFIRMWARE_STATUS, null, null, null);		
    }
    
    /**
     * Get a list of NetworkInterface configurations.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces
     * 
     * @return ResponseStatus (NetworkInterfaceList)
     * @throws CameraException
     */
    public ResponseStatus getSystemNetworkInterfaces() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_NETWORK_INTERFACES, null, null, null);		
    }
    
    /**
     * Get a the NetworkInterface object for the specified interface.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]
     * 
     * @param channelId - Network Interface Id
     * @return ResponseStatus (NetworkInterface)
     * @throws CameraException
     */
    public ResponseStatus getSystemNetworkInterfaces(String networkInterfaceId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_NETWORK_INTERFACES_ID, null, networkInterfaceId, null); 	
    }
    
    /**
     * Set a the NetworkInterface configuration for the specified interface.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]
     * 
     * @param networkInterfaceId - Network Interface Id
     * @param networkInterface - NetworkInterface
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemNetworkInterfaces(String networkInterfaceId, NetworkInterface networkInterface) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_NETWORK_INTERFACES_ID, null, networkInterfaceId, networkInterface); 	
    }
    
    /**
     * Get the IPAddress configuration for the specified Network Interface.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]/ipAddress
     * 
     * @param networkInterfaceId - Network Interface Id.
     * @return ResponseStatus (IPAddress)
     * @throws CameraException
     */
    public ResponseStatus getSystemNetworkInterfacesIpAddress(String networkInterfaceId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_NETWORK_INTERFACES_ID_IPADDRESS, null, networkInterfaceId, null); 	
    }
    
    /**
     * Set the IPAddress configuration for the specified Network Interface from the provided IPAddress object.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]/ipAddress
     * 
     * @param networkInterfaceId - Network Interface Id.
     * @param ipAddress - IPAddress
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemNetworkInterfacesIpAddress(String networkInterfaceId, IPAddress ipAddress) throws CameraException { 
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_NETWORK_INTERFACES_ID_IPADDRESS, null, networkInterfaceId, ipAddress); 	
    }
    
    /**
     * Get the Wireless configuration for the specified Network Interface.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]/Wireless
     * 
     * @param networkInterfaceId - Network Interface Id.
     * @return ResponseStatus (Wireless)
     * @throws CameraException
     */
    public ResponseStatus getSystemNetworkInterfacesWireless(String networkInterfaceId) throws CameraException { 
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS, null, networkInterfaceId, null); 	
    }
    
    /**
     * Set the Wireless configuration for the specified Network Interface from the provided Wireless object.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]/Wireless
     * 
     * @param networkInterfaceId - Network Interface Id.
     * @param wireless - Wireless
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemNetworkInterfacesWireless(String networkInterfaceId, Wireless wireless) throws CameraException { 
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS, null, networkInterfaceId, wireless); 	
    }
    
    /**
     * Get wireless Network Interface status.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]/Wireless/status 
     *  
     * @param networkInterfaceId - Id of the Network Interface.
     * @param forceRefresh - "true" to force the device to collect new status before returning response.
     * @return ResponseStatus (WirelessNetworkStatus )
     * @throws CameraException
     */
    public ResponseStatus getSystemNetworkInterfacesWirelessStatus(String networkInterfaceId, String forceRefresh) throws CameraException {
    	
    	if (forceRefresh != null) {
    		String[] parameters = {API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS_STATUS_PARAMETER_FORCEREFRESH, forceRefresh};
        	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS_STATUS, parameters, networkInterfaceId, null); 	
    	}
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS_STATUS, null, networkInterfaceId, null); 	
    } 
    
    /**
     * Get wireless Network Interface status (no refresh option).
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]/Wireless/status 
     *  
     * @param networkInterfaceId - Id of the Network Interface.
     * @return ResponseStatus (WirelessNetworkStatus )
     * @throws CameraException
     */
    public ResponseStatus getSystemNetworkInterfacesWirelessStatus(String networkInterfaceId) throws CameraException {
    	return getSystemNetworkInterfacesWirelessStatus(networkInterfaceId, null); 	
    }

    
    /**
     * Get the IEE802.1x configuration for the specified Network Interface.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]/ieee802.1x
     * 
     * @param networkInterfaceId - Network Interface Id.
     * @return ResponseStatus (IEEE8021X)
     * @throws CameraException
     */
    public ResponseStatus getSystemNetworkInterfacesIee8021x(String networkInterfaceId) throws CameraException { 
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_NETWORK_INTERFACES_ID_IEE8021X, null, networkInterfaceId, null); 	
    }
    
    /**
     * Set the IEE802.1x configuration for the specified Network Interface from the provided IEEE8021X object.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]/ieee802.1x
     * 
     * @param networkInterfaceId - Network Interface Id.
     * @param ieee8021x - IEEE8021X
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemNetworkInterfacesIee8021x(String networkInterfaceId, IEEE8021X ieee8021x) throws CameraException { 
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_NETWORK_INTERFACES_ID_IEE8021X, null, networkInterfaceId, ieee8021x); 	
    }
    
    /**
     * Retrieves the discovery configuration for the specified Network Interface.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]/Discovery
     * 
     * @param networkInterfaceId - Network Interface Id.
     * @return ResponseStatus (Discovery)
     * @throws CameraException
     */
    public ResponseStatus getSystemNetworkInterfacesDiscovery(String networkInterfaceId) throws CameraException { 
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_NETWORK_INTERFACES_ID_DISCOVERY, null, networkInterfaceId, null); 	
    }
    
    /**
     * Sets the discovery configuration for the specified Network Interface from the provided Discovery object.
     * <p>
     * Command: /OpenHome/System/Network/Interfaces/[id]/Discovery
     * 
     * @param networkInterfaceId
     * @param discovery - Discovery
     * @return ResponseStatus 
     * @throws CameraException
     */
    public ResponseStatus setSystemNetworkInterfacesDiscovery(String networkInterfaceId, Discovery discovery) throws CameraException { 
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_NETWORK_INTERFACES_ID_DISCOVERY, null, networkInterfaceId, discovery); 	
    }
    
    /**
     * Gets the list of all hardware audio inputs.
     * <p>
     * Command: /OpenHome/System/Audio/channels
     * 
     * @return ResponseStatus (AudioChannelList)
     * @throws CameraException
     */
    public ResponseStatus getAudioChannels() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_AUDIO_CHANNELS, null, null, null);
    }
    
    /**
     * Get the configuration of the specified audio channel.
     * <p>
     * Command: /OpenHome/System/Audio/channels/[id]
     * 
     * @param channelId - Audio channel id
     * @return ResponseStatus (AudioChannel)
     * @throws CameraException
     */
    public ResponseStatus getAudioChannels(String channelId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_AUDIO_CHANNELS_ID, null, channelId, null);
    }
    
    /**
     * Set the configuration of the specified audio channel.
     * <p>
     * Command: /OpenHome/System/Audio/channels/[id]
     * 
     * @param channelId - Audio channel id
     * @param audioChannel - AudioChannel
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setAudioChannels(String channelId, AudioChannel audioChannel) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_AUDIO_CHANNELS_ID, null, channelId, audioChannel);
    }
    
    /**
     * Get a VideoInput object which is a wrapper for the VideoInputChannelList which is
     * a list of VideoInputChannel objects that contain the configuration of each video channel.
     * <p>
     * Command: /OpenHome/System/Video/inputs
     * 
     * @return ResponseStatus (VideoInput)
     * @throws CameraException
     */
    public ResponseStatus getVideoInputs() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_VIDEO_INPUTS, null, null, null);
    }
    
    /**
     * Get a VideoInputChannelList which is a list of VideoInputChannel objects
     * that contain the configuration of each video channel.
     * <p>
     * Command: /OpenHome/System/Video/inputs/channels
     * 
     * @return ResponseStatus (VideoInputChannelList)
     * @throws CameraException
     */
    public ResponseStatus getVideoInputsChannels() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_VIDEO_INPUTS_CHANNELS, null, null, null);
    }
    
    /**
     * Get a VideoInputChannel object for the specified video channel.
     * <p>
     * Command: /OpenHome/System/Video/inputs/channels/[id]
     * 
     * @param channelId - Id of video input channel.
     * @return ResponseStatus (VideoInputChannel)
     * @throws CameraException
     */
    public ResponseStatus getVideoInputsChannels(String channelId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_VIDEO_INPUTS_CHANNELS_ID, null, channelId, null);
    }
    
    /**
     * Set the configuration of a video input channel with the specified channel Id from a VideoInputChannel object.
     * <p>
     * Command: /OpenHome/System/Video/inputs/channels/[id]
     * 
     * @param channelId - Id of video input channel.
     * @param videoInputChannel - VideoInputChannel.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setVideoInputsChannels(String channelId, VideoInputChannel videoInputChannel) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_VIDEO_INPUTS_CHANNELS_ID, null, channelId, videoInputChannel);
    }
    
    /**
     * Get the current status of all streaming sessions.
     * <p>
     * Command: /OpenHome/Streaming/status
     * 
     * @return ResponseStatus (StreamingSessionStatusList)
     * @throws CameraException
     */
    public ResponseStatus getStreamingStatus() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_STREAMING_STATUS, null, null, null);
    }
    
    /**
     * Get the current status of the specified streaming session.
     * <p>
     * Command: /OpenHome/Streaming/[id]/status
     * 
     * @param channelId
     * @return
     * @throws CameraException
     */
    public ResponseStatus getStreamingStatus(String channelId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_STREAMING_ID_STATUS, null, channelId, null);
    }
    
    /**
     * Get capabilities of a the specified streaming channel and streaming protocol.
     * <p>
     * Command: /OpenHome/Streaming/[id]/capabilities
     *
     * @param channelId - Id of streaming channel.
     * @return ResponseStatus (StreamingCapabilities)
     * @throws CameraException
     */
    public ResponseStatus getStreamingChannelCapabilities(String channelId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_STREAMING_CHANNELS_ID_CAPABILITIES, null, channelId, null);
    }
    
    /**
     * Get a list of all streaming channels and their configuration.
     * <p>
     * Command: /OpenHome/Streaming/channels
     *
     * @return ResponseStatus (StreamingChannelList)
     * @throws CameraException
     */
    public ResponseStatus getStreamingChannels() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_STREAMING_CHANNELS, null, null, null);
    }
    
    
    /**
     * Get the configuration of the specified streaming channel.
     * <p>
     * Command: /OpenHome/Streaming/channels/[id]
     *
     * @param channelId - id of the streaming channel.
     * @return ResponseStatus (StreaminChannel)
     * @throws CameraException
     */
    public ResponseStatus getStreamingChannels(String channelId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_STREAMING_CHANNELS_ID, null, channelId, null);
    }
    
    /**
     * Set the configuration of the specified streaming channel.
     * <p>
     * Command: /OpenHome/Streaming/channels/[id]
     *
     * @param streamingChannel - StreamingChannel
     * @param channelId  - id of the streaming channel.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus putStreamingChannels(String channelId, StreamingChannel streamingChannel) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_STREAMING_CHANNELS_ID, null, channelId, streamingChannel);
    }
    
    /**
     * Delete the specified streaming channel.
     * <p>
     * Command: /OpenHome/Streaming/channels/[id]
     *
     * @param streamingChannel - StreamingChannel
     * @param channelId  - id of the streaming channel.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus deleteStreamingChannels(String channelId, StreamingChannel streamingChannel) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_DELETE, API_STREAMING_CHANNELS_ID, null, channelId, streamingChannel);
    }
    
    /**
     * Initiate a video clip upload from the default channel.
     * <p>
     * Command: /OpenHome/Streaming/Channels/[id]/Video/Upload
     * 
     * @param mediaUpload - MediaUpload
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus streamingChannelsIdVideoUpload(MediaUpload mediaUpload) throws CameraException {
    	return streamingChannelsIdVideoUpload(Constants.DEFAULT_STREAMING_CHANNEL_ID, mediaUpload);
    }
    
    /**
     * Initiate a video clip upload from the specified channel.
     * <p>
     * Command: /OpenHome/Streaming/Channels/[id]/Video/Upload
	 *
     * @param mediaUpload - MediaUpload
     * @param channelId - id of the streaming channel.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus streamingChannelsIdVideoUpload(String channelId, MediaUpload mediaUpload) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_STREAMING_CHANNELS_ID_VIDEO_UPLOAD, null, channelId, mediaUpload);
    }
    
    /**
     * Initiate a snapshot upload from the default channel.
     * <p>
     * Command: /OpenHome/Streaming/Channels/[id]/Picture/Upload
	 *
     * @param mediaUpload - MediaUpload
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus streamingChannelsIdPictureUpload(MediaUpload mediaUpload) throws CameraException {
    	return streamingChannelsIdPictureUpload(Constants.DEFAULT_STREAMING_CHANNEL_ID, mediaUpload);
    }
    
    /**
     * Initiate a snapshot upload from the specified channel.
     * <p>
     * Command: /OpenHome/Streaming/Channels/[id]/Picture/Upload
	 *
     * @param mediaUpload - MediaUpload
     * @param channelId - id of the streaming channel.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus streamingChannelsIdPictureUpload(String channelId, MediaUpload mediaUpload) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_STREAMING_CHANNELS_ID_PICTURE_UPLOAD, null, channelId, mediaUpload);
    }
    
    /**
     * Get a snapshot from the specified channel.
     * <p>
     * Command: /OpenHome/Streaming/Channels/[id]/Picture
	 *
     * @param channelId - id of the streaming channel.
     * @param videoResolutionWidth - picture width in pixels.
     * @param videoResolutionHeight - picture height in pixels.
     * @param fixedQuality - 1 to 100, 100 is the highest quality. 
     * @return ResponseStatus (byte stream)
     * @throws CameraException
     */
    public ResponseStatus streamingChannelsIdPicture(String channelId, String videoResolutionWidth, String videoResolutionHeight, String fixedQuality) throws CameraException {
    	
    	if (videoResolutionWidth == null && videoResolutionHeight == null && fixedQuality == null) {
    		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_STREAMING_CHANNELS_ID_PICTURE, null, channelId, null);
    	}
    	
    	List<String> parameters = new ArrayList<String>();
    	    	
    	if (videoResolutionWidth != null) {
    		parameters.add(API_STREAMING_CHANNELS_ID_PICTURE_PARAMETER_VIDEORESOLUTIONWIDTH);
    		parameters.add(videoResolutionWidth);
    	}
    	
    	if (videoResolutionHeight != null) {
    		parameters.add(API_STREAMING_CHANNELS_ID_PICTURE_PARAMETER_VIDEORESOLUTIONHEIGHT);
    		parameters.add(videoResolutionHeight);
    	}
    	
    	if (fixedQuality != null) {
    		parameters.add(API_STREAMING_CHANNELS_ID_PICTURE_PARAMETER_FIXEDQUALITY);
    		parameters.add(fixedQuality);
    	}
    	
		return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_STREAMING_CHANNELS_ID_PICTURE, (String[]) parameters.toArray(), null, null);
    	
    }
    
    /**
     * Get a MediaTunnelList of MediaTunnel objects for active media tunnels.
     * <p>
     * Command: /OpenHome/Streaming/Channels/MediaTunnel
	 *
     * @return ResponseStatus (MediaTunnelList)
     * @throws CameraException
     */
    public ResponseStatus getStreamingChannelMediaTunnel() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_STREAMING_MEDIATUNNEL, null, null, null);
    }
    
    /**
     * Get a MediaTunnel objects for the specified media tunnel.
     * <p>
     * Command: /OpenHome/Streaming/Channels/MediaTunnel/[id]/status
	 *
     * @param tunnelId - Id of active media tunnel
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus getStreamingChannelMediaTunnel(String tunnelId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_STREAMING_MEDIATUNNEL_ID_STATUS, null, tunnelId, null);
    }
    
    /**
     * Create a media tunnel from camera to a location specified in the MediaTunnel object.
     * <p>
     * Command: /OpenHome/Streaming/Channels/MediaTunnel/Create
	 *
     * @param mediaTunnel - CreateMediaTunnel
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus streamingChannelMediaTunnelCreate(CreateMediaTunnel createMediaTunnel) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_STREAMING_MEDIATUNNEL_CREATE, null, null, createMediaTunnel);
    }
    
    /**
     * Destroy an active media tunnel.
     * <p>
     * Command: /OpenHome/Streaming/Channels/MediaTunnel/[id]/destroy
	 *
     * @param tunnelId - Id of active media tunnel
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus streamingChannelMediaTunnelDestroy(String tunnelId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_STREAMING_MEDIATUNNEL_ID_DESTROY, null, tunnelId, null);
    }
    
    /**
     * Get the system configuration objects in the ConfigFile wrapper.
     * 
     * Command: /OpenHome/System/ConfigurationData/configFile
     * 
     * @return ResponseStatus (ConfigFile)
     * @throws CameraException
     */
    public ResponseStatus getSystemConfiguration() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_CONFIGURATIONDATA_CONFIGFILE, null, null, null);
    }
    
    
    /**
     * Set the system configuration from the objects in the ConfigFile wrapper.
     * <p>
     * Command: /OpenHome/System/ConfigurationData/configFile
     * 
     * @param configFile - ConfigFile
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemConfiguration(ConfigFile configFile) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_CONFIGURATIONDATA_CONFIGFILE, null, null, configFile);
    }
    
    /**
     * Get the system timer configuration.
     * 
     * Command: /OpenHome/System/ConfigurationData/Timers
     * 
     * @return ResponseStatus (ConfigTimers)
     * @throws CameraException
     */
    public ResponseStatus getSystemConfigurationTimers() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SYSTEM_CONFIGURATIONDATA_TIMERS, null, null, null);
    }
    
    /**
     * Set the system timer configuration.
     * 
     * Command: /OpenHome/System/ConfigurationData/Timers
     * 
     * @param configTimers - ConfigTimers
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setSystemConfigurationTimers(ConfigTimers configTimers) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SYSTEM_CONFIGURATIONDATA_TIMERS, null, null, configTimers);
    }
    
    /**
     * Get the PTZ configuration of all channels.
     * 
     * @return ResponseStatus (PTZChannelList)
     * @throws CameraException
     */
    public ResponseStatus getPtzChannels() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_PTZ_CHANNELS, null, null, null);
    }
    
    /**
     * Get the PTZ configuration of the specified channel.
     * 
     * @param channelId - Id of the PTZ channel.
     * @return ResponseStatus (PTZChannel)
     * @throws CameraException
     */
    public ResponseStatus getPtzChannels(String channelId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_PTZ_CHANNELS, null, channelId, null);
    }
    
    /**
     * Returns the current camera position in the PTZStatus object.
     * 
     * @param channelId - Id of the PTZ channel.
     * @return ResponseStatus (PTZStatus)
     * @throws CameraException
     */
    public ResponseStatus getPtzChannelsStatus(String channelId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_PTZ_CHANNELS_ID_STATUS, null, channelId, null);
    }
    
    /**
     * Sets the camera's current position as its Home Position.
     * 
     * @param channelId - Id of the PTZ channel.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setPtzChannelsHomePosition(String channelId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_PTZ_CHANNELS_ID_HOMEPOSITION, null, channelId, null);
    }
    
    /**
     * Move the camera to a position relative to home as specified in the PTZData object.
     * 
     * @param channelId - Id of the PTZ channel.
     * @param ptzData - PTZData
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setPtzChannelsRelative(String channelId, PTZData ptzData) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_PTZ_CHANNELS_ID_RELATIVE, null, channelId, ptzData);
    }
    
    /**
     * Move the camera to the absolute position specified in the PTZData object.
     * 
     * @param channelId - Id of the PTZ channel.
     * @param ptzData - PTZData
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setPtzChannelsAbsolute(String channelId, PTZData ptzData) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_PTZ_CHANNELS_ID_ABSOLUTE, null, channelId, ptzData);
    }
    
    /**
     * Get UserList object which is a wrapper for a list of Account objects which describe a user's id, password and role.
     * <p>
     * Command: /OpenHome/Security/AAA/Accounts
     * 
     * @return ResponseStatus (UserList)
     * @throws CameraException
     */
    public ResponseStatus getUserAccounts() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SECURITY_AAA_ACCOUNTS, null, null, null);
    }
    
    /**
     * Set user's name, password and role from a list of Account objects in the UserList wrapper object.
     * <p>
     * Command: /OpenHome/Security/AAA/Accounts
     * 
     * @param userList - UserList
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus putUserAccounts(UserList userList) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SECURITY_AAA_ACCOUNTS, null, null, userList);
    }
    
    /**
     * Get user's name, password and role for a specified Account id.
     * <p>
     * Command: /OpenHome/Security/AAA/Accounts/[id]
     * 
     * @param accountId - Account id.
     * @return ResponseStatus (Account)
     * @throws CameraException
     */
    public ResponseStatus getUserAccounts(String accountId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SECURITY_AAA_ACCOUNTS, null, accountId, null);
    }
    
    /**
     * Set user's name, password and role for a specified Account id.
     * <p>
     * Command: /OpenHome/Security/AAA/Accounts/[id]
     * 
     * @param accountId - Id of user account.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus putUserAccounts(String accountId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SECURITY_AAA_ACCOUNTS_ID, null, accountId, null);
    }
    
    /**
     * Delete the specified Account.
     * <p>
     * Command: /OpenHome/Security/AAA/Accounts/[id]
     * 
     * @param accountId - Id of user account.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus deleteUserAccounts(String accountId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_DELETE, API_SECURITY_AAA_ACCOUNTS_ID, null, accountId, null);
    }
    
    /**
     * Set user's name, password and role for a specified Account id.
     * <p>
     * Command: /OpenHome/Security/AAA/Accounts/[id]
     * 
     * @param account
     * @param accountId - Id of user account
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus putUserAccounts(String accountId, Account account) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SECURITY_AAA_ACCOUNTS_ID, null, accountId, account);
    }
    
    /**
     * Get authorization credential.
     * <p>
     * Command: /OpenHome/Security/Authorization
     * 
     * @return ResponseStatus (AuthorizationInfo)
     * @throws CameraException
     */
    public ResponseStatus getSecurityAuthorization() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_SECURITY_AUTHORIZATION, null, null, null);
    }
    
    /**
     * Set authorization credential.
     * <p>
     * Command: /OpenHome/Security/Authorization
     * 
     * @param authorizationInfo - AuthorizationInfo
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus putSecurityAuthorization(AuthorizationInfo authorizationInfo) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_SECURITY_AUTHORIZATION, null, null, authorizationInfo);
    }
    
    /**
     * Returns the EventNotification object which is a wrapper for a list of 
     * EventTriggers and EventNotificaytionMehtods.
     * <p>
     * Command: /OpenHome/Event
     * 
     * @return ResponseStatus (EventNotification)
     * @throws CameraException
     */
    public ResponseStatus getEvent() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_EVENT, null, null, null);
    }
    
    /**
     * Get an EventTriggerList which is a wrapper for a list of EventTrigger(s)
     * for this device.
     * <p>
     * Command: /OpenHome/Event/triggers
     * 
     * @return ResponseStatus (EventTriggerList)
     * @throws CameraException
     */
    public ResponseStatus getEventTriggers() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_EVENT_TRIGGERS, null, null, null);
    }
    
    /**
     * Reconfigure the EventTrigger(s) in the specified EventTriggerList.
     * <p>
     * Command: /OpenHome/Event/triggers
     * 
     * @param eventTriggerList
     * @return
     * @throws CameraException
     */
    public ResponseStatus setEventTriggers(EventTriggerList eventTriggerList) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_EVENT_TRIGGERS, null, null, eventTriggerList);
    }
    
    /**
     * Add an EventTrigger.
     * <p>
     * Command: /OpenHome/Event/triggers
     * 
     * @param eventTrigger
     * @return
     * @throws CameraException
     */
    public ResponseStatus addEventTriggers(EventTrigger eventTrigger) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_CUSTOM_EVENT_TRIGGERS, null, null, eventTrigger);
    }
    
    /**
     * Get an EventTrigger with the specified ID.
     *  <p>
     * Command: /OpenHome/Event/triggers/[id]
     * 
     * @param triggerId - Id of the EventTrigger.
     * @return ResponseStatus (EventTrigger)
     * @throws CameraException
     */
    public ResponseStatus getEventTriggers(String triggerId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_EVENT_TRIGGERS_ID, null, triggerId, null);
    }
    
    /**
     * Update the EventTrigger with the specified ID.
     *  <p>
     * Command: /OpenHome/Event/triggers/[id]
     * 
     * @param triggerId - Id of the EventTrigger.
     * @param eventTrigger - EventTrigger
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setEventTriggers(String triggerId, EventTrigger eventTrigger) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_EVENT_TRIGGERS_ID, null, triggerId, eventTrigger);
    }
    
    /**
     * Delete the EventTrigger with the specified ID.
     *  <p>
     * Command: /OpenHome/Event/triggers/[id]
     * 
     * @param triggerId - Id of the EventTrigger.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus deleteEventTriggers(String triggerId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_DELETE, API_CUSTOM_EVENT_TRIGGERS_ID, null, triggerId, null);
    }
    
    /**
     * Get an EventTriggerNotificationList from the EventTrigger specified by the ID.
     * The EventTriggerNotificationList is a wrapper for a list of EventTriggerNotification objects.
     *  <p>
     * Command: /OpenHome/Event/triggers/[id]/notifications
     * 
     * @param triggerId - Id of the EventTrigger.
     * @return ResponseStatus (EventTriggerNotificationList)
     * @throws CameraException
     */
    public ResponseStatus getEventTriggerNotifications(String triggerId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS, null, triggerId, null);
    }
    
    /**
     * Update the EventTriggerNotification(s) in the EventTrigger specified by ID.
     * <p>
     * Command: /OpenHome/Event/triggers/[id]/notifications
     * 
     * @param triggerId - Id of the EventTrigger.
     * @param eventTriggerNotificationList - EventTriggerNotificationList.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setEventTriggerNotifications(String triggerId, EventTriggerNotificationList eventTriggerNotificationList) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS, null, triggerId, eventTriggerNotificationList);
    }
    
    /**
     * Add an EventTriggerNotification to the EventTrigger specified by ID.
     * <p>
     * Command: /OpenHome/Event/triggers/[id]/notifications
     * 
     * @param triggerId - Id of the EventTrigger.
     * @param eventTriggerNotification - EventTriggerNotification
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus addEventTriggerNotifications(String triggerId, EventTriggerNotification eventTriggerNotification) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS, null, triggerId, null);
    }
    
    /**
     * Get the EventTriggerNotification specified by ID.
     * <p>
     * Command: /OpenHome/Event/triggers/[id]/notifications/[id]
     * 
     * @param triggerId - Id of the EventTrigger.
     * @param notifyId - Id of the EventTriggerNotification.
     * @return ResponseStatus (EventTriggerNotification)
     * @throws CameraException
     */
    public ResponseStatus getEventTriggerNotifications(String triggerId, String notifyId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS_NOTIFYID, null, new String[] {triggerId, notifyId}, null);
    }
    
    /**
     * Update the EventTriggerNotification specified by ID.
     * <p>
     * Command: /OpenHome/Event/triggers/[id]/notifications/[id]
     * 
     * @param triggerId - Id of the EventTrigger.
     * @param notifyId - Id of the EventTriggerNotification.
     * @param eventTriggerNotification - EventTriggerNotification
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setEventTriggerNotifications(String triggerId, String notifyId, EventTriggerNotification eventTriggerNotification) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS_NOTIFYID, null, new String[] {triggerId, notifyId}, eventTriggerNotification);
    }
    
    /**
     * Delete the EventTriggerNotification specified by ID.
     * <p>
     * Command: /OpenHome/Event/triggers/[id]/notifications/[id]
     * 
     * @param triggerId - Id of the EventTrigger.
     * @param notifyId - Id of the EventTriggerNotification.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus deleteEventTriggerNotifications(String triggerId, String notifyId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_DELETE, API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS_NOTIFYID, null, new String[] {triggerId, notifyId}, null);
    }
    
    /**
     * Get the notifications configuration for this device from the 
     * EventNotificationMethods object.
     * <p>
     * Command: /OpenHome/Event/notification/methods
     * 
     * @return ResponseStatus (EventNotificationMethods)
     * @throws CameraException
     */
    public ResponseStatus getEventNotificationMethods() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_EVENT_NOTIFICATION_METHODS, null, null, null);
    }
    
    /**
     * Update the notifications configuration specified by eventNotificationMethods. 
     * <p>
     * Command: /OpenHome/Event/notification/methods 
     * 
     * @param eventNotificationMethods - EventNotificationMethods
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setEventNotificationMethods(EventNotificationMethods eventNotificationMethods) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_EVENT_NOTIFICATION_METHODS, null, null, eventNotificationMethods);
    }
    
    /**
     * Get the host configurations for notifications for this device.
     * <p>
     * Command: /OpenHome/Event/notification/host
     * 
     * @return ResponseStatus (HostNotificationList)
     * @throws CameraException
     */
    public ResponseStatus getEventNotificationHost() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_EVENT_NOTIFICATION_HOST, null, null, null);
    }
    
    /**
     * Update the host configurations for notifications for this device.
     * <p>
     * Command: /OpenHome/Event/notification/host
     * 
     * @param hostNotificationList
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setEventNotificationHost(HostNotificationList hostNotificationList) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_EVENT_NOTIFICATION_HOST, null, null, hostNotificationList);
    }
    
    /**
     * Get the host notifications configurations for a specified id.
     * <p>
     * Command: /OpenHome/Event/notification/host/[id]
     * 
     * @param notificationId - Id of the notification.
     * @return ResponseStatus (HostNotification)
     * @throws CameraException
     */
    public ResponseStatus getEventNotificationHost(String notificationId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_EVENT_NOTIFICATION_HOST_ID, null, notificationId, null);
    }
    
    /**
     * Update the host notifications configurations for a specified id.
     * <p>
     * Command: /OpenHome/Event/notification/host/[id]
     * 
     * @param notificationId - Id of the notification.
     * @param hostNotification - HostNotification
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setEventNotificationHost(String notificationId, HostNotification hostNotification) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_EVENT_NOTIFICATION_HOST_ID, null, notificationId, hostNotification);
    }
    
    /**
     * Delete the host notifications configurations for a specified id.
     * <p>
     * Command: /OpenHome/Event/notification/host/[id]
     * 
     * @param notificationId - Id of the notification. 
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus deleteEventNotificationHost(String notificationId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_DELETE, API_CUSTOM_EVENT_NOTIFICATION_HOST_ID, null, notificationId, null);
    }
    
    /**
     * Get the PIR Motion detection configuration for all video input channels.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/PIR
     * 
     * @return ResponseStatus (MotionDetectionList)
     * @throws CameraException
     */
    public ResponseStatus getMotionDetectionPir() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_MOTIONDETECTION_PIR, null, null, null);
    }
    
    /**
     * Get the video Motion detection configuration for all video input channels.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/video
     * 
     * @return ResponseStatus (MotionDetectionList)
     * @throws CameraException
     */
    public ResponseStatus getMotionDetectionVideo() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_MOTIONDETECTION_VIDEO, null, null, null);
    }
    
    /**
     * Get the PIR Motion detection configuration for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/PIR/[id]
     * 
     * @param id - video input channel id.
     * @return ResponseStatus (MotionDetection)
     * @throws CameraException
     */
    public ResponseStatus getMotionDetectionPir(String id) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_MOTIONDETECTION_PIR_ID, null, id, null);
    }
    
    /**
     * Update the PIR Motion detection configuration for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/PIR/[id]
     * 
     * @param id - video input channel id.
     * @param motionDetection - MotionDetection
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setMotionDetectionPir(String id, MotionDetection motionDetection) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_MOTIONDETECTION_PIR_ID, null, id, motionDetection);
    }
    
    /**
     * Get the video Motion detection configuration for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/video/[id]
     * 
     * @param id - video input channel id.
     * @return ResponseStatus (MotionDetection)
     * @throws CameraException
     */
    public ResponseStatus getMotionDetectionVideo(String id) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_MOTIONDETECTION_VIDEO_ID, null, id, null);
    }
    
    /**
     * Update the video Motion detection configuration for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/video/[id]
     * 
     * @param id - video input channel id.
     * @param motionDetection - MotionDetection
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setMotionDetectionVideo(String id, MotionDetection motionDetection) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_MOTIONDETECTION_VIDEO_ID, null, id, motionDetection);
    }
    
    /**
     * Get all video motion detection regions configuration for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/video/[id]/regions
     * 
     * @param id - video input channel id.
     * @return ResponseStatus (MotionDetectionRegionList)
     * @throws CameraException
     */
    public ResponseStatus getMotionDetectionVideoRegions(String id) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS, null, id, null);
    }
    
    /**
     * Update all video motion detection regions configuration for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/video/[id]/regions
     * 
     * @param id - video input channel id.
     * @param motionDetectionRegionList - MotionDetectionRegionList
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setMotionDetectionVideoRegions(String id, MotionDetectionRegionList motionDetectionRegionList) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS, null, id, motionDetectionRegionList);
    }
    
    /**
     * Add a video motion detection region for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/video/[id]/regions
     * 
     * @param id - video input channel id.
     * @param motionDetectionRegion - MotionDetectionRegion
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus addMotionDetectionVideoRegions(String id, MotionDetectionRegion motionDetectionRegion) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_POST, API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS, null, id, motionDetectionRegion);
    }
    
    /**
     * Get the specified video motion detection region configuration for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/video/[id]/regions/[id]
     * 
     * @param id - video input channel id.
     * @param regionId - Region id.
     * @return ResponseStatus (MotionDetectionRegion)
     * @throws CameraException
     */
    public ResponseStatus getMotionDetectionVideoRegions(String id, String regionId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS_ID, null, new String[] {id, regionId}, null);
    }
    
    /**
     * Update the specified video motion detection region configuration for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/video/[id]/regions/[id]
     * 
     * @param id - video input channel id.
     * @param regionId - Region id.
     * @param motionDetectionRegion - MotionDetectionRegion
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setMotionDetectionVideoRegions(String id, String regionId, MotionDetectionRegion motionDetectionRegion) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS_ID, null, new String[] {id, regionId}, motionDetectionRegion);
    }
    
    /**
     * Delete the specified video motion detection region configuration for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/video/[id]/regions/[id]
     * 
     * @param id - video input channel id.
     * @param regionId - Region id.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus deleteMotionDetectionVideoRegions(String id, String regionId) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_DELETE, API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS_ID, null, new String[] {id, regionId}, null);
    }
    
    /**
     * Get the video motion detection capabilities for the specified video input channel.
     * <p>
     * Command: /OpenHome/Event/MotionDetection/video/[id]/capabilities
     * 
     * @param id  - video input channel id.
     * @return ResponseStatus (MotionDetectionCapabilities)
     * @throws CameraException
     */
    public ResponseStatus getMotionDetectionVideoCapabilities(String id) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_MOTIONDETECTION_VIDEO_ID_CAPABILITIES, null, id, null);
    }
    
    /**
     * Get the sound detection configuration for all audio input channels.
     * <p>
     * Command: /OpenHome/Event/SoundDetection
     * 
     * @return ResponseStatus (SoundDetectionList)
     * @throws CameraException
     */
    public ResponseStatus getSoundDetection() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_SOUNDDETECTION, null, null, null);
    }
    
    /**
     * Get the sound detection configuration for the specified audio input channel.
     * <p>
     * Command: /OpenHome/Event/SoundDetection/[id]
     * 
     * @param id  - audio input channel id.
     * @return ResponseStatus (SoundDetection)
     * @throws CameraException
     */
    public ResponseStatus getSoundDetection(String id) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_SOUNDDETECTION_ID, null, id, null);
    }
    
    /**
     * Update the sound detection configuration for the specified audio input channel.
     * <p>
     * Command: /OpenHome/Event/SoundDetection/[id]
     * 
     * @param id  - audio input channel id.
     * @param soundDetection
     * @return
     * @throws CameraException
     */
    public ResponseStatus setSoundDetection(String id, SoundDetection soundDetection) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_SOUNDDETECTION_ID, null, id, soundDetection);
    }
    
    /**
     * Get the temperature detection configuration for all temperature change detection channels.
     * <p>
     * Command: /OpenHome/Event/TemperatureDetection
     * 
     * @return ResponseStatus (TemperatureDetectionList)
     * @throws CameraException
     */
    public ResponseStatus getTemperatureDetection() throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_TEMPERATUREDETECTION, null, null, null);
    }
    
    /**
     * Get the temperature detection configuration for the specified temperature change detection channel.
     * <p>
     * Command: /OpenHome/Event/TemperatureDetection/[id]
     * 
     * @param id - temperature change detection channel id.
     * @return ResponseStatus (TemperatureDetection)
     * @throws CameraException
     */
    public ResponseStatus getTemperatureDetection(String id) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_TEMPERATUREDETECTION_ID, null, id, null);
    }
    
    /**
     * Update the temperature detection configuration for the specified temperature change detection channel.
     * <p>
     * Command: /OpenHome/Event/TemperatureDetection/[id]
     * 
     * @param id - temperature change detection channel id.
     * @param temperatureDetection
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setTemperatureDetection(String id, TemperatureDetection temperatureDetection) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_PUT, API_CUSTOM_TEMPERATUREDETECTION_ID, null, id, temperatureDetection);
    }
    
    /**
     * Get the current temperature from the specified temperature change detection channel.
     * <p>
     * Command: /OpenHome/Event/TemperatureDetection/[id]/current
     * 
     * @param id - temperature change detection channel id.
     * @return ResponseStatus (CurrentTemperature)
     * @throws CameraException
     */
    public ResponseStatus getTemperatureDetectionCurrent(String id) throws CameraException {
    	return issueCameraCommand(Constants.HTTP_REQUEST_GET, API_CUSTOM_TEMPERATUREDETECTION_ID_CURRENT, null, id, null);
    }
    
    /**
     * Build the HTTP request and send it to the device.
     * 
     * @param requestType - (required) HTTP request type (GET, PUT, POST, DELETE)
     * @param locationURL - (required) server (device) URL
     * @param command - (required) restful path of service
     * @param parameters - (may be null) Http request parameters as an array ordered as "name","value" pairs.
     * @param parameterObject - (may be null) object to be xml encoded and passed with http request as a parameter (e.g. &ltDeviceInfo&gt).
     * @return - ResponseStatus
     * @throws CameraException
     */
    public abstract ResponseStatus issueCameraCommand(String requestType, String command, String[] parameters, Object ids, Object parameterObject) throws CameraException;

}
