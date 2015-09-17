package com.icontrol.ohcm;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * This class is a facade for HttpsURLConnection.
 * 
 * @author wek
 * @version 1.0
 */
public class OpenHomeConnectionImpl implements OpenHomeConnection {
	
	private HttpsURLConnection httpsURLConnection;
	
	private URL url;
	
	OpenHomeConnectionImpl(String url) throws Exception {
		
		this.url = new URL(url);
		httpsURLConnection = (HttpsURLConnection) this.url.openConnection();

	}

	public void connect() throws Exception {
		httpsURLConnection.connect();
	}

	public void disconnect() {
		httpsURLConnection.disconnect();
	}

	public int getResponseCode() throws Exception {
		return httpsURLConnection.getResponseCode();
	}

	public InputStream getErrorStream() throws Exception {
		return httpsURLConnection.getErrorStream();
	}

	public InputStream getInputStream() throws Exception {
		return httpsURLConnection.getInputStream();
	}

	public HttpsURLConnection getHttpsURLConnection() {
		return httpsURLConnection;
	}

}
