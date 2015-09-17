package com.icontrol.ohcm;

public class Constants {
	
	//HTTP request type values
	public static final String HTTP_REQUEST_GET = "GET";
	public static final String HTTP_REQUEST_PUT = "PUT";
	public static final String HTTP_REQUEST_POST = "POST";
	public static final String HTTP_REQUEST_DELETE = "DELETE";
	
	public static final String DEFAULT_CHANNEL_ID = "0";
	public static final String DEFAULT_NETWORKINTERFACE_IPADDRESS_ID = "0";
	public static final String DEFAULT_NETWORKINTERFACE_WIRELESS_ID = "1";
	public static final String DEFAULT_NETWORKINTERFACE_IEEE8021X_ID = "2";
	public static final String DEFAULT_STREAMING_CHANNEL_ID = "0";
	public static final String DEFAULT_MEDIA_TUNNEL_ID = "0";
	public static final String DEFAULT_AUDIO_INPUT_CHANNEL_ID = "0";
	public static final String DEFAULT_VIDEO_INPUT_CHANNEL_ID = "0";
	public static final String DEFAULT_USER_ACCOUNT_ID = "0";
	public static final String DEFAULT_MEDIA_UPLOAD_ID = "1234567890";
	public static final String ID_REPLACEMENT_STRING = "[uid]";
	public static final String NOTIFYID_REPLACEMENT_STRING = "[notifyid]";
	public static final String REGIONID_REPLACEMENT_STRING = "[regionid]";
	public static final String AUTHORIZATION = "Authorization";
	public static final String BASIC = "Basic ";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String APPLICATION_XML = "application/xml";
	public static final String ERROR = "ERROR";
	public static final String OK = "OK";
	public static final String STRING_0 = "0";
	
	public static final String MESSAGE_REQUEST_PARAMETERS_IMPROPERLY_SPECIFIED = "Request parameters are improperly specified";
	public static final String MESSAGE_REQUIRED_PARAMETER_IS_NULL = "Required parameter is null";
	public static final String MESSAGE_COMMAND_NOT_SUPPORTED = "Command is not supported";
	public static final String MESSAGE_MALFORMED_URL = "Malformed URL";
	public static final String MESSAGE_CONNECTION_ERROR = "Connection error";
	public static final String MESSAGE_ERROR_ACCESSING_NETWORKINTERFACE_OBJECT = "Error accessing NetworkIterface Object";

}
