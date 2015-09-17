package com.icontrol.ohcm;

/**
 * 
 * @author wek
 *
 */
public interface OpenHomeConnectionFactory {
	
	/**
	 * Create an OpenHomeConnetion.
	 * 
	 * @param requestType - HTTP Request Type (GET, PUT, POST, DELETE)
	 * @param url - Complete url including command path and substitutions<br>
	 * 				e.g. https://192.168.1.23/OpenHome/System/Audio/channels/0
	 * @param parameters - String array of parameter pairs e.g. {"linger", "10"}
	 * @param parameterObject - Serialized string to be sent as a parameter with the command<br> 
	 *                          e.g. DeviceInfo with PUT /OpenHome/System/deviceInfo. 
	 * @param userid - user id to be used for authentication.
	 * @param password - password corresponding to the userid.
	 * @param connectTimeout - connections time out in milliseconds.
	 * @param readTimeout - read time out in milliseconds.
	 * @return OpenHomeConnection
	 */
	public OpenHomeConnection getOpenHomeConnection(
			String requestType, 
			String url, 
			String[] parameters, 
			String parameterObject,
			String userid,
			String password,
			int connectTimeout,
			int readTimeout);
}
