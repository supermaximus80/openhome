package com.icontrol.ohcm;

import java.io.InputStream;

/**
 * This file defines an interface for a class that emulates some behaviors of the javax.net.ssl.HttpsURLConnection class.
 * 
 * @author wek
 * @version 1.0
 */
public interface OpenHomeConnection {
	
	
	/**
	 * Opens a communications link to the resource referenced by this URL, if such a connection has not already been established. 
	 * 
	 * @throws IOException - if an I/O error occurs while opening the connection.
	 * @throws SocketTimeoutException - if the timeout expires before the connection can be established.
	 */
	public void connect() throws Exception;
	
	/**
	 * Indicates that this connection is no longer accepting requests. 
	 *  
	 */
	public void disconnect();
	
	/**
	 * Gets the status code from an HTTP response message.
	 * 
	 * @return - int value of the status code e.g. 200: OK.
	 * @throws IOException - if an error occurred connecting to the server.
	 */
	public int getResponseCode() throws Exception;
	
	/**
	 * Returns the error stream if the connection failed but the server sent error data 
	 * such as an html stream. 
	 * 
	 * @return - an error stream if any.
	 */
	public InputStream getErrorStream() throws Exception;
	
	/**
	 * Returns an input stream that reads from this open connection. 
	 * A SocketTimeoutException can be thrown when reading from the 
	 * returned input stream if the read timeout expires before data 
	 * is available for read.
	 * 
	 * @return InputStream 
	 * @throws IOException - if an I/O error occurs while creating the input stream.
	 * @throws UnknownServiceException - if the protocol does not support input.
	 */
	public InputStream getInputStream() throws Exception;
	

	/**
	 * Returns a String representation of this URL connection. 
	 * 
	 * @return String (URL)
	 */
	public String toString();

}
