package com.icontrol.ohcm;


import java.util.HashMap;
import java.util.Map;

import com.icontrol.openhome.data.ResponseStatus;

/**
 * Defines the methods and commands used to communicate with the Open Home camera.
 * @author wek
 * @version 1.0
 */
public interface OpenHomeCameraDriver {
	
	/*
	 * Default root user and password.
	 */
	public static final String DEFAULT_ROOT_USER = "administrator";
	public static final String DEFAULT_ROOT_PASSWORD = "";
	
	/*
	 * The published command paths.
	 */
    public static final String API_API = "/openhome/api";
    
    public static final String API_SYSTEM_REBOOT = "/openhome/system/reboot";
    public static final String API_SYSTEM_UPDATEFIRMWARE = "/openhome/system/updatefirmware";
    public static final String API_SYSTEM_UPDATEFIRMWARE_STATUS = "/openhome/system/updatefirmware/status";
    public static final String API_SYSTEM_CONFIGURATIONDATA_CONFIGFILE = "/openhome/system/configurationdata/configfile";
    public static final String API_SYSTEM_CONFIGURATIONDATA_TIMERS = "/openhome/system/configurationdata/timers";
    public static final String API_SYSTEM_FACTORYRESET = "/openhome/system/factoryreset";
    public static final String API_SYSTEM_FACTORYRESET_PARAMETER_MODE = "mode";
    public static final String API_SYSTEM_DEVICEINFO = "/openhome/system/deviceinfo";
    public static final String API_SYSTEM_TIME = "/openhome/system/time";
    public static final String API_SYSTEM_TIME_TIMEZONE = "/openhome/system/time/timezone";
    public static final String API_SYSTEM_TIME_NTPSERVERS = "/openhome/system/time/ntpservers";
    public static final String API_SYSTEM_TIME_NTPSERVERS_ID = "/openhome/system/time/ntpservers/[uid]";
    public static final String API_SYSTEM_LOGGING = "/openhome/system/logging";
    public static final String API_SYSTEM_LOGGING_LOGDATA = "/openhome/system/logging/logdata";
    public static final String API_SYSTEM_LOGGING_LOGDATA_PARAMETER_SINCE = "since";
    public static final String API_SYSTEM_HOST_SERVER = "/openhome/system/host/server";
    public static final String API_SYSTEM_HISTORY = "/openhome/system/history";
    public static final String API_SYSTEM_HISTORY_PARAMETER_SINCECOMMAND = "sinceCommand";
    public static final String API_SYSTEM_HISTORY_PARAMETER_SINCENOTIFICATION = "sinceNotification";
    public static final String API_SYSTEM_HISTORY_CONFIGURATION = "/openhome/system/history/configuration";
    public static final String API_SYSTEM_POLL_NOTIFICATIONS = "/openhome/system/poll/notifications";
    public static final String API_SYSTEM_POLL_NOTIFICATIONS_PARAMETER_LINGER = "linger";
    public static final String API_SYSTEM_PING = "/openhome/system/ping";
    public static final String API_SYSTEM_NETWORK_INTERFACES = "/openhome/system/network/interfaces";
    public static final String API_SYSTEM_NETWORK_INTERFACES_ID = "/openhome/system/network/interfaces/[uid]";
    public static final String API_SYSTEM_NETWORK_INTERFACES_ID_IPADDRESS = "/openhome/system/network/interfaces/[uid]/ipaddress";
    public static final String API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS = "/openhome/system/network/interfaces/[uid]/wireless";
    public static final String API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS_STATUS = "/openhome/system/network/interfaces/[uid]/wireless/status";
    public static final String API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS_STATUS_PARAMETER_FORCEREFRESH = "forceRefresh";
    public static final String API_SYSTEM_NETWORK_INTERFACES_ID_IEE8021X = "/openhome/system/network/interfaces/[uid]/ieee802.1x";
    public static final String API_SYSTEM_NETWORK_INTERFACES_ID_DISCOVERY = "/openhome/system/network/interfaces/[uid]/discovery";

    public static final String API_SYSTEM_AUDIO_CHANNELS = "/openhome/system/audio/channels";
    public static final String API_SYSTEM_AUDIO_CHANNELS_ID = "/openhome/system/audio/channels/[uid]";

    public static final String API_SYSTEM_VIDEO_INPUTS = "/openhome/system/video/inputs";
    public static final String API_SYSTEM_VIDEO_INPUTS_CHANNELS = "/openhome/system/video/inputs/channels";
    public static final String API_SYSTEM_VIDEO_INPUTS_CHANNELS_ID = "/openhome/system/video/inputs/channels/[uid]";

    public static final String API_SECURITY_UPDATESSLCERTIFICATE_CLIENT = "/openhome/security/updatesslcertificate/client";
    public static final String API_SECURITY_UPDATESSLCERTIFICATE_SERVER = "/openhome/security/updatesslcertificate/server";

    public static final String API_SECURITY_AAA_ACCOUNTS = "/openhome/security/aaa/accounts";
    public static final String API_SECURITY_AAA_ACCOUNTS_ID = "/openhome/security/aaa/accounts/[uid]";
    public static final String API_SECURITY_AUTHORIZATION = "/openhome/security/authorization";
    
    public static final String API_STREAMING_CHANNELS = "/openhome/streaming/channels";
    public static final String API_STREAMING_CHANNELS_ID = "/openhome/streaming/channels/[uid]";
    public static final String API_STREAMING_CHANNELS_ID_CAPABILITIES = "/openhome/streaming/channels/[uid]/capabilities";
    public static final String API_STREAMING_STATUS = "/openhome/streaming/status";
    public static final String API_STREAMING_ID_STATUS = "/openhome/streaming/[uid]/status";
    public static final String API_STREAMING_CHANNELS_ID_STATUS = "/openhome/streaming/channels/[uid]/status";
    public static final String API_STREAMING_CHANNELS_ID_VIDEO_UPLOAD = "/openhome/streaming/channels/[uid]/video/upload";
    public static final String API_STREAMING_CHANNELS_ID_PICTURE_UPLOAD = "/openhome/streaming/channels/[uid]/picture/upload";
    public static final String API_STREAMING_CHANNELS_ID_REQUESTKEYFRAME = "/openhome/streaming/channels/[uid]/requestkeyframe";
    public static final String API_STREAMING_CHANNELS_ID_PICTURE_PARAMETER_VIDEORESOLUTIONWIDTH = "videoResolutionWidth";
    public static final String API_STREAMING_CHANNELS_ID_PICTURE_PARAMETER_VIDEORESOLUTIONHEIGHT = "videoResolutionHeight";
    public static final String API_STREAMING_CHANNELS_ID_PICTURE_PARAMETER_FIXEDQUALITY = "fixedQuality";

    /*
    public static final String API_STREAMING_TRACKS_ID_HTTP = "/openhome/streaming/tracks/[uid]/http";
    public static final String API_STREAMING_TRACKS_ID_RTSP = "/openhome/streaming/tracks/[uid]/rtsp";
    public static final String API_STREAMING_TRACKS_ID_VIDEO_UPLOAD = "/openhome/streaming/tracks/[uid]/video/upload";
    */

    public static final String API_STREAMING_CHANNELS_ID_PICTURE = "/openhome/streaming/channels/[uid]/picture";
    public static final String API_STREAMING_CHANNELS_ID_MJPEG = "/openhome/streaming/channels/[uid]/mjpeg";
    public static final String API_STREAMING_CHANNELS_ID_RTSP = "/openhome/streaming/channels/[uid]/rtsp";
    public static final String API_STREAMING_CHANNELS_ID_FLV = "/openhome/streaming/channels/[uid]/flv";
    public static final String API_STREAMING_CHANNELS_ID_HLS_PLAYLIST = "/openhome/streaming/channels/[uid]/hls/playlist";
    public static final String API_STREAMING_CHANNELS_ID_HLS_SEGMENTID = "/openhome/streaming/channels/[uid]/hls/[segmentid]";

    public static final String API_STREAMING_MEDIATUNNEL = "/openhome/streaming/mediatunnel";
    public static final String API_STREAMING_MEDIATUNNEL_ID_STATUS = "/openhome/streaming/mediatunnel/[uid]/status";
    public static final String API_STREAMING_MEDIATUNNEL_CREATE = "/openhome/streaming/mediatunnel/create";
    public static final String API_STREAMING_MEDIATUNNEL_ID_DESTROY = "/openhome/streaming/mediatunnel/[uid]/destroy";

    public static final String API_PTZ_CHANNELS = "/openhome/ptz/channels";
    public static final String API_PTZ_CHANNELS_ID = "/openhome/ptz/channels/[uid]";
    public static final String API_PTZ_CHANNELS_ID_HOMEPOSITION = "/openhome/ptz/channels/[uid]/homeposition";
    public static final String API_PTZ_CHANNELS_ID_RELATIVE = "/openhome/ptz/channels/[uid]/relative";
    public static final String API_PTZ_CHANNELS_ID_ABSOLUTE = "/openhome/ptz/channels/[uid]/absolute";
    public static final String API_PTZ_CHANNELS_ID_STATUS = "/openhome/ptz/channels/[uid]/status";

    public static final String API_CUSTOM_EVENT = "/openhome/event";
    public static final String API_CUSTOM_EVENT_TRIGGERS = "/openhome/event/triggers";
    public static final String API_CUSTOM_EVENT_TRIGGERS_ID = "/openhome/event/triggers/[uid]";
    public static final String API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS = "/openhome/event/triggers/[uid]/notifications";
    public static final String API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS_NOTIFYID = "/openhome/event/triggers/[uid]/notifications/[notifyid]";
    public static final String API_CUSTOM_EVENT_NOTIFICATION_METHODS = "/openhome/event/notification/methods";
    public static final String API_CUSTOM_EVENT_NOTIFICATION_HOST = "/openhome/event/notification/host";
    public static final String API_CUSTOM_EVENT_NOTIFICATION_HOST_ID = "/openhome/event/notification/host/[uid]";

    public static final String API_CUSTOM_MOTIONDETECTION_PIR = "/openhome/event/motiondetection/pir";
    public static final String API_CUSTOM_MOTIONDETECTION_PIR_ID = "/openhome/event/motiondetection/pir/[uid]";
    public static final String API_CUSTOM_MOTIONDETECTION_VIDEO = "/openhome/event/motiondetection/video";
    public static final String API_CUSTOM_MOTIONDETECTION_VIDEO_ID = "/openhome/event/motiondetection/video/[uid]";
    public static final String API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS = "/openhome/event/motiondetection/video/[uid]/regions";
    public static final String API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS_ID = "/openhome/event/motiondetection/video/[uid]/regions/[regionid]";
    public static final String API_CUSTOM_MOTIONDETECTION_VIDEO_ID_CAPABILITIES = "/openhome/event/motiondetection/video/[uid]/capabilities";
    public static final String API_CUSTOM_SOUNDDETECTION = "/openhome/event/sounddetection";
    public static final String API_CUSTOM_SOUNDDETECTION_ID = "/openhome/event/sounddetection/[uid]";
    public static final String API_CUSTOM_TEMPERATUREDETECTION = "/openhome/event/temperaturedetection";
    public static final String API_CUSTOM_TEMPERATUREDETECTION_ID = "/openhome/event/temperaturedetection/[uid]";
    public static final String API_CUSTOM_TEMPERATUREDETECTION_ID_CURRENT = "/openhome/event/temperaturedetection/[uid]/current";

    /*
    public static final String API_CONTENTMGMT_API = "/openhome/contentmgmt/api";
    public static final String API_CONTENTMGMT_PROFILE = "/openhome/contentmgmt/profile";
    public static final String API_CONTENTMGMT_RECORD_STORAGEMOUNTS = "/openhome/contentmgmt/record/storagemounts";
    public static final String API_CONTENTMGMT_RECORD_STORAGEMOUNTS_ID = "/openhome/contentmgmt/record/storagemounts/[uid]";
    public static final String API_CONTENTMGMT_RECORD_PROFILE = "/openhome/contentmgmt/record/profile";
    public static final String API_CONTENTMGMT_RECORD_TRACKS = "/openhome/contentmgmt/record/tracks";
    public static final String API_CONTENTMGMT_RECORD_TRACKS_ID = "/openhome/contentmgmt/record/tracks/[uid]";
    public static final String API_CONTENTMGMT_RECORD_CONTROL_MANUAL_START_TRACKS_ID = "/openhome/contentmgmt/control/manual/start/tracks/[uid]";
    public static final String API_CONTENTMGMT_RECORD_CONTROL_MANUAL_STOP_TRACKS_ID = "/openhome/contentmgmt/control/manual/stop/tracks/[uid]";
    public static final String API_CONTENTMGMT_RECORD_CONTROL_LOCKS = "/openhome/contentmgmt/record/control/locks";
    public static final String API_CONTENTMGMT_RECORD_CONTROL_LOCKS_ID = "/openhome/contentmgmt/record/control/locks/[uid]";
    public static final String API_CONTENTMGMT_SCHEDULES = "/openhome/contentmgmt/schedules";
    public static final String API_CONTENTMGMT_SCHEDULES_ID = "/openhome/contentmgmt/schedules/[uid]";
    public static final String API_CONTENTMGMT_SEARCH = "/openhome/contentmgmt/search";
    public static final String API_CONTENTMGMT_SEARCH_DESCRIPTION = "/openhome/contentmgmt/search/description";
    public static final String API_CONTENTMGMT_SEARCH_PROFILE = "/openhome/contentmgmt/search/profile";
    public static final String API_CONTENTMGMT_STATUS_CHANNELS = "/openhome/contentmgmt/status/channels";
    public static final String API_CONTENTMGMT_STATUS_SOURCES = "/openhome/contentmgmt/status/sources";
    public static final String API_CONTENTMGMT_STATUS_TRACKS = "/openhome/contentmgmt/status/tracks";
    public static final String API_CONTENTMGMT_STATUS_VOLUME = "/openhome/contentmgmt/status/volume";
    public static final String API_CONTENTMGMT_STATUS_CHANNELS_ID = "/openhome/contentmgmt/status/channels/[uid]";
    public static final String API_CONTENTMGMT_STATUS_SOURCES_ID = "/openhome/contentmgmt/status/sources/[uid]";
    public static final String API_CONTENTMGMT_STATUS_TRACKS_ID = "/openhome/contentmgmt/status/tracks/[uid]";
    public static final String API_CONTENTMGMT_STATUS_VOLUME_ID = "/openhome/contentmgmt/status/volume/[uid]";
    */

    /*
     * The default command path mapping.
     */
    @SuppressWarnings("serial")
	public static Map<String, String> defaultCommandMap = new HashMap<String, String>(){{
    	put(API_API , API_API);
    	put(API_SYSTEM_REBOOT , API_SYSTEM_REBOOT);
        put(API_SYSTEM_UPDATEFIRMWARE , API_SYSTEM_UPDATEFIRMWARE);
        put(API_SYSTEM_UPDATEFIRMWARE_STATUS , API_SYSTEM_UPDATEFIRMWARE_STATUS);
        put(API_SYSTEM_CONFIGURATIONDATA_CONFIGFILE , API_SYSTEM_CONFIGURATIONDATA_CONFIGFILE);
        put(API_SYSTEM_CONFIGURATIONDATA_TIMERS , API_SYSTEM_CONFIGURATIONDATA_TIMERS);
        put(API_SYSTEM_FACTORYRESET , API_SYSTEM_FACTORYRESET);
        put(API_SYSTEM_DEVICEINFO , API_SYSTEM_DEVICEINFO);
        put(API_SYSTEM_TIME , API_SYSTEM_TIME);
        put(API_SYSTEM_TIME_TIMEZONE , API_SYSTEM_TIME_TIMEZONE);
        put(API_SYSTEM_TIME_NTPSERVERS , API_SYSTEM_TIME_NTPSERVERS);
        put(API_SYSTEM_TIME_NTPSERVERS_ID , API_SYSTEM_TIME_NTPSERVERS_ID);
        put(API_SYSTEM_LOGGING , API_SYSTEM_LOGGING);
        put(API_SYSTEM_LOGGING_LOGDATA , API_SYSTEM_LOGGING_LOGDATA);
        put(API_SYSTEM_HOST_SERVER , API_SYSTEM_HOST_SERVER);
        put(API_SYSTEM_HISTORY , API_SYSTEM_HISTORY);
        put(API_SYSTEM_HISTORY_CONFIGURATION , API_SYSTEM_HISTORY_CONFIGURATION);
        put(API_SYSTEM_POLL_NOTIFICATIONS , API_SYSTEM_POLL_NOTIFICATIONS);
        put(API_SYSTEM_PING , API_SYSTEM_PING);
        put(API_SYSTEM_NETWORK_INTERFACES , API_SYSTEM_NETWORK_INTERFACES);
        put(API_SYSTEM_NETWORK_INTERFACES_ID , API_SYSTEM_NETWORK_INTERFACES_ID);
        put(API_SYSTEM_NETWORK_INTERFACES_ID_IPADDRESS , API_SYSTEM_NETWORK_INTERFACES_ID_IPADDRESS);
        put(API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS , API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS);
        put(API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS_STATUS , API_SYSTEM_NETWORK_INTERFACES_ID_WIRELESS_STATUS);
        put(API_SYSTEM_NETWORK_INTERFACES_ID_IEE8021X , API_SYSTEM_NETWORK_INTERFACES_ID_IEE8021X);
        put(API_SYSTEM_NETWORK_INTERFACES_ID_DISCOVERY , API_SYSTEM_NETWORK_INTERFACES_ID_DISCOVERY);
        put(API_SYSTEM_AUDIO_CHANNELS , API_SYSTEM_AUDIO_CHANNELS);
        put(API_SYSTEM_AUDIO_CHANNELS_ID , API_SYSTEM_AUDIO_CHANNELS_ID);
        put(API_SYSTEM_VIDEO_INPUTS , API_SYSTEM_VIDEO_INPUTS);
        put(API_SYSTEM_VIDEO_INPUTS_CHANNELS , API_SYSTEM_VIDEO_INPUTS_CHANNELS);
        put(API_SYSTEM_VIDEO_INPUTS_CHANNELS_ID , API_SYSTEM_VIDEO_INPUTS_CHANNELS_ID);
        put(API_SECURITY_UPDATESSLCERTIFICATE_CLIENT , API_SECURITY_UPDATESSLCERTIFICATE_CLIENT);
        put(API_SECURITY_UPDATESSLCERTIFICATE_SERVER , API_SECURITY_UPDATESSLCERTIFICATE_SERVER);
        put(API_SECURITY_AAA_ACCOUNTS , API_SECURITY_AAA_ACCOUNTS);
        put(API_SECURITY_AAA_ACCOUNTS_ID , API_SECURITY_AAA_ACCOUNTS_ID);
        put(API_SECURITY_AUTHORIZATION , API_SECURITY_AUTHORIZATION);
        put(API_STREAMING_CHANNELS , API_STREAMING_CHANNELS);
        put(API_STREAMING_CHANNELS_ID , API_STREAMING_CHANNELS_ID);
        put(API_STREAMING_CHANNELS_ID_CAPABILITIES , API_STREAMING_CHANNELS_ID_CAPABILITIES);
        put(API_STREAMING_STATUS , API_STREAMING_STATUS);   
        put(API_STREAMING_ID_STATUS , API_STREAMING_ID_STATUS);   
        put(API_STREAMING_CHANNELS_ID_STATUS , API_STREAMING_CHANNELS_ID_STATUS);
        put(API_STREAMING_CHANNELS_ID_RTSP , API_STREAMING_CHANNELS_ID_RTSP); 
        put(API_STREAMING_CHANNELS_ID_FLV , API_STREAMING_CHANNELS_ID_FLV);
        put(API_STREAMING_CHANNELS_ID_FLV , API_STREAMING_CHANNELS_ID_FLV);
        put(API_STREAMING_CHANNELS_ID_VIDEO_UPLOAD , API_STREAMING_CHANNELS_ID_VIDEO_UPLOAD);
        put(API_STREAMING_CHANNELS_ID_PICTURE_UPLOAD , API_STREAMING_CHANNELS_ID_PICTURE_UPLOAD);
        put(API_STREAMING_CHANNELS_ID_REQUESTKEYFRAME , API_STREAMING_CHANNELS_ID_REQUESTKEYFRAME);
        put(API_STREAMING_CHANNELS_ID_MJPEG, API_STREAMING_CHANNELS_ID_MJPEG);
        put(API_STREAMING_CHANNELS_ID_PICTURE , API_STREAMING_CHANNELS_ID_PICTURE);
        put(API_STREAMING_CHANNELS_ID_PICTURE_UPLOAD , API_STREAMING_CHANNELS_ID_PICTURE_UPLOAD);
        put(API_STREAMING_CHANNELS_ID_HLS_PLAYLIST , API_STREAMING_CHANNELS_ID_HLS_PLAYLIST);
        put(API_STREAMING_CHANNELS_ID_HLS_SEGMENTID , API_STREAMING_CHANNELS_ID_HLS_SEGMENTID);
        put(API_STREAMING_MEDIATUNNEL , API_STREAMING_MEDIATUNNEL);
        put(API_STREAMING_MEDIATUNNEL_ID_STATUS , API_STREAMING_MEDIATUNNEL_ID_STATUS);
        put(API_STREAMING_MEDIATUNNEL_CREATE , API_STREAMING_MEDIATUNNEL_CREATE);
        put(API_STREAMING_MEDIATUNNEL_ID_DESTROY , API_STREAMING_MEDIATUNNEL_ID_DESTROY);
/*
        put(API_STREAMING_TRACKS_ID_HTTP , API_STREAMING_TRACKS_ID_HTTP);
        put(API_STREAMING_TRACKS_ID_RTSP , API_STREAMING_TRACKS_ID_RTSP);
        put(API_STREAMING_TRACKS_ID_HLS_PLAYLIST , API_STREAMING_TRACKS_ID_HLS_PLAYLIST);
        put(API_STREAMING_TRACKS_ID_HLS_SEGMENTID , API_STREAMING_TRACKS_ID_HLS_SEGMENTID);
        put(API_STREAMING_TRACKS_ID_VIDEO_UPLOAD , API_STREAMING_TRACKS_ID_VIDEO_UPLOAD);
*/
        put(API_PTZ_CHANNELS , API_PTZ_CHANNELS);
        put(API_PTZ_CHANNELS_ID , API_PTZ_CHANNELS_ID);
        put(API_PTZ_CHANNELS_ID_HOMEPOSITION , API_PTZ_CHANNELS_ID_HOMEPOSITION);
        put(API_PTZ_CHANNELS_ID_RELATIVE , API_PTZ_CHANNELS_ID_RELATIVE);
        put(API_PTZ_CHANNELS_ID_ABSOLUTE , API_PTZ_CHANNELS_ID_ABSOLUTE);
        put(API_PTZ_CHANNELS_ID_STATUS , API_PTZ_CHANNELS_ID_STATUS);
        put(API_CUSTOM_MOTIONDETECTION_PIR , API_CUSTOM_MOTIONDETECTION_PIR);
        put(API_CUSTOM_MOTIONDETECTION_PIR_ID , API_CUSTOM_MOTIONDETECTION_PIR_ID);
        put(API_CUSTOM_MOTIONDETECTION_VIDEO , API_CUSTOM_MOTIONDETECTION_VIDEO);
        put(API_CUSTOM_MOTIONDETECTION_VIDEO_ID , API_CUSTOM_MOTIONDETECTION_VIDEO_ID);
        put(API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS , API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS);
        put(API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS_ID , API_CUSTOM_MOTIONDETECTION_VIDEO_ID_REGIONS_ID);
        put(API_CUSTOM_MOTIONDETECTION_VIDEO_ID , API_CUSTOM_MOTIONDETECTION_VIDEO_ID);
        put(API_CUSTOM_MOTIONDETECTION_VIDEO_ID_CAPABILITIES , API_CUSTOM_MOTIONDETECTION_VIDEO_ID_CAPABILITIES);
        put(API_CUSTOM_SOUNDDETECTION , API_CUSTOM_SOUNDDETECTION);
        put(API_CUSTOM_SOUNDDETECTION_ID , API_CUSTOM_SOUNDDETECTION_ID);
        put(API_CUSTOM_TEMPERATUREDETECTION , API_CUSTOM_TEMPERATUREDETECTION);
        put(API_CUSTOM_TEMPERATUREDETECTION_ID , API_CUSTOM_TEMPERATUREDETECTION_ID);
        put(API_CUSTOM_TEMPERATUREDETECTION_ID_CURRENT , API_CUSTOM_TEMPERATUREDETECTION_ID_CURRENT);
        put(API_CUSTOM_EVENT , API_CUSTOM_EVENT);
        put(API_CUSTOM_EVENT_TRIGGERS , API_CUSTOM_EVENT_TRIGGERS);
        put(API_CUSTOM_EVENT_TRIGGERS_ID , API_CUSTOM_EVENT_TRIGGERS_ID);
        put(API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS , API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS);
        put(API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS_NOTIFYID , API_CUSTOM_EVENT_TRIGGERS_ID_NOTIFICATIONS_NOTIFYID);
        put(API_CUSTOM_EVENT_NOTIFICATION_METHODS , API_CUSTOM_EVENT_NOTIFICATION_METHODS);
        put(API_CUSTOM_EVENT_NOTIFICATION_HOST , API_CUSTOM_EVENT_NOTIFICATION_HOST);
        put(API_CUSTOM_EVENT_NOTIFICATION_HOST_ID , API_CUSTOM_EVENT_NOTIFICATION_HOST_ID);
        
        
/*
        put(API_CONTENTMGMT_API , API_CONTENTMGMT_API);
        put(API_CONTENTMGMT_PROFILE , API_CONTENTMGMT_PROFILE);
        put(API_CONTENTMGMT_RECORD_STORAGEMOUNTS , API_CONTENTMGMT_RECORD_STORAGEMOUNTS);
        put(API_CONTENTMGMT_RECORD_STORAGEMOUNTS_ID , API_CONTENTMGMT_RECORD_STORAGEMOUNTS_ID);
        put(API_CONTENTMGMT_RECORD_PROFILE , API_CONTENTMGMT_RECORD_PROFILE);
        put(API_CONTENTMGMT_RECORD_TRACKS , API_CONTENTMGMT_RECORD_TRACKS);
        put(API_CONTENTMGMT_RECORD_TRACKS_ID , API_CONTENTMGMT_RECORD_TRACKS_ID);
        put(API_CONTENTMGMT_RECORD_CONTROL_MANUAL_START_TRACKS_ID , API_CONTENTMGMT_RECORD_CONTROL_MANUAL_START_TRACKS_ID);
        put(API_CONTENTMGMT_RECORD_CONTROL_MANUAL_STOP_TRACKS_ID , API_CONTENTMGMT_RECORD_CONTROL_MANUAL_STOP_TRACKS_ID);
        put(API_CONTENTMGMT_RECORD_CONTROL_LOCKS , API_CONTENTMGMT_RECORD_CONTROL_LOCKS);
        put(API_CONTENTMGMT_RECORD_CONTROL_LOCKS_ID , API_CONTENTMGMT_RECORD_CONTROL_LOCKS_ID);
        put(API_CONTENTMGMT_SCHEDULES , API_CONTENTMGMT_SCHEDULES);
        put(API_CONTENTMGMT_SCHEDULES_ID , API_CONTENTMGMT_SCHEDULES_ID);
        put(API_CONTENTMGMT_SEARCH , API_CONTENTMGMT_SEARCH);
        put(API_CONTENTMGMT_SEARCH_DESCRIPTION , API_CONTENTMGMT_SEARCH_DESCRIPTION);
        put(API_CONTENTMGMT_SEARCH_PROFILE , API_CONTENTMGMT_SEARCH_PROFILE);
        put(API_CONTENTMGMT_STATUS_CHANNELS , API_CONTENTMGMT_STATUS_CHANNELS);
        put(API_CONTENTMGMT_STATUS_SOURCES , API_CONTENTMGMT_STATUS_SOURCES);
        put(API_CONTENTMGMT_STATUS_TRACKS , API_CONTENTMGMT_STATUS_TRACKS);
        put(API_CONTENTMGMT_STATUS_VOLUME , API_CONTENTMGMT_STATUS_VOLUME);
        put(API_CONTENTMGMT_STATUS_CHANNELS_ID , API_CONTENTMGMT_STATUS_CHANNELS_ID);
        put(API_CONTENTMGMT_STATUS_SOURCES_ID , API_CONTENTMGMT_STATUS_SOURCES_ID);
        put(API_CONTENTMGMT_STATUS_TRACKS_ID , API_CONTENTMGMT_STATUS_TRACKS_ID);
        put(API_CONTENTMGMT_STATUS_VOLUME_ID , API_CONTENTMGMT_STATUS_VOLUME_ID);
*/
    }};
    
    /**
     * All commands are issued to the camera in this general format.
	 *
     * @param requestType - (required) HTTP request type (GET, PUT, POST, DELETE)
     * @param command - (required) restful path of service
     * @param parameters - (may be null) Http request parameters as an array ordered as "name","value" pairs.
     * @param id - if there is an id for this request (e.g. streaming channel id) that is part of the request path.
     * @param parameterObject - (may be null) object to be xml encoded and passed with http request as a parameter (e.g. DeviceInfo).
     * @return - ResponseStatus 
     * @throws CameraException
     */
    public ResponseStatus issueCameraCommand(String requestType, String command, String[] parameters, Object ids, Object parameterObject) throws CameraException;
 

}

