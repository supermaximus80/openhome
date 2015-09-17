package com.icontrol.ohcm;

import java.io.DataOutputStream;
import java.security.InvalidParameterException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionUtilities implements OpenHomeConnectionFactory {
	
    //Slf4j Logger object
    private static Logger logger = LoggerFactory.getLogger(ConnectionUtilities.class.getName());
    
    public ConnectionUtilities()
    {
    }
    

	public OpenHomeConnection getOpenHomeConnection(String requestType,
														String url, 
														String[] parameters, 
														String parameterObject,
														String userid,
														String password,
														int connectTimeout,
														int readTimeout) {
		
		OpenHomeConnectionImpl connection = null;
    	
    	try {

			connection = new OpenHomeConnectionImpl(url);
			connection.getHttpsURLConnection().addRequestProperty(Constants.AUTHORIZATION, Constants.BASIC + new String(Base64.encodeBase64((userid + ":" + password).getBytes())));
			connection.getHttpsURLConnection().setRequestMethod(requestType);
			connection.getHttpsURLConnection().setRequestProperty(Constants.CONTENT_TYPE, Constants.APPLICATION_XML);
			connection.getHttpsURLConnection().setDoInput(true);
			connection.getHttpsURLConnection().setDoOutput(true);
			connection.getHttpsURLConnection().setConnectTimeout(connectTimeout);
			connection.getHttpsURLConnection().setReadTimeout(readTimeout);
			connection.getHttpsURLConnection().setHostnameVerifier(new VerifyHostName());
			
    		if (parameterObject == null) {
    			connection.getHttpsURLConnection().setRequestProperty(Constants.CONTENT_LENGTH, Constants.STRING_0);
    		}
			
			
			if (parameters != null && parameters.length > 1 && parameters.length == (parameters.length / 2) * 2 ) { 
				for (int i = 0; i < parameters.length ; i += 2) {
					connection.getHttpsURLConnection().addRequestProperty(parameters[i], parameters[i+1]);
					logger.info("Parameter: " + parameters[i] + " Value: " + parameters[i+1]);
				}

			}
			else if (parameters != null) {
				throw new InvalidParameterException(Constants.MESSAGE_REQUEST_PARAMETERS_IMPROPERLY_SPECIFIED);
			}
			
			if (parameterObject != null) {
				DataOutputStream xmlStream = new DataOutputStream(connection.getHttpsURLConnection().getOutputStream());
				xmlStream.writeBytes(parameterObject);
				xmlStream.close();
			}
			
    	} catch (Exception e) {
    		logger.error(Constants.MESSAGE_CONNECTION_ERROR + " : " + e.getMessage(), e.getCause());
    	}
    	
    	return connection;
		
	}
	
    static class VerifyHostName implements HostnameVerifier {

    	public boolean verify(String hostname, SSLSession session) {
    		
    		logger.info("hostname: " + hostname);
    		return true;
    	}
    	
    }
}
