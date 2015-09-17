/*
 */
package com.icontrol.openhomesimulator.camera;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.MalformedURLException;

public class RtspURL
{
	/**
	 * Constructor
	 *
	 */
	public RtspURL()
	{
		m_bSecure = false;
		m_scheme = RTSP;
        m_host = null;
		m_hostPort = 0;
		m_path = null;
	}

    /*
        parse rtsp url
        example:  rtsp://quicktime.tc.columbia.edu:554/users/lrf10/movies/sixties.mov
     */
    public RtspURL(String urlStr) throws MalformedURLException
    {
        if (urlStr==null || urlStr.length() == 0)
            throw new MalformedURLException("Invalid urlstr="+urlStr) ;
        int pos = urlStr.indexOf("://");
        if (pos == -1)
            throw new MalformedURLException("Invalid urlstr="+urlStr) ;
        m_scheme = urlStr.substring(0, pos);
        if (RTSPS.equalsIgnoreCase(m_scheme))
            m_bSecure = true;
        else
            m_bSecure = false;

        pos += "://".length();
        int pos2 = urlStr.indexOf(":", pos) ;
        if (pos2 > 0) {   // port is explicit
            m_host = urlStr.substring(pos, pos2);
            pos = pos2 + 1;
            pos2 = urlStr.indexOf("/", pos) ;
            if (pos2 == -1)
                throw new MalformedURLException("Invalid urlstr="+urlStr) ;
            String portStr = urlStr.substring(pos, pos2);
            m_hostPort = Integer.parseInt(portStr);
        } else {
            m_hostPort = 554;
            pos2 = urlStr.indexOf("/", pos) ;
            if (pos2 == -1)
                throw new MalformedURLException("Invalid urlstr="+urlStr) ;
            m_host = urlStr.substring(pos, pos2);
        }

        // get path
        pos = pos2 + 1;
        m_path = urlStr.substring(pos);

        //log.debug("rtspURLstr="+urlStr+" decodedURL="+toString());
    }

	/**
	 * Override URIImpl methods
	 */

	/**
	 * Returns the value of the "scheme" of this URI, for example "rtsp", "rtspu" or "rtsps".
	 *
	 * @return the scheme parameter of the URI
	 */
	// Overriding the URIImpl method
	public String getScheme()
	{
		return m_scheme;
	}

	/**
	 * Set the URI scheme
	 *
	 * @param scheme The scheme
	 */
	public void setScheme( String scheme) throws MalformedURLException
	{
		if( scheme.equals( RTSP))
		{
			m_bSecure = false;
			m_scheme = RTSP;
		}
		else if( scheme.equals( RTSPU))
		{
			m_bSecure = false;
			m_scheme = RTSPU;
		}
		else if( scheme.equals( RTSPS))
		{
			m_bSecure = true;
			m_scheme = RTSPS;
		}
		else
		{
			throw new MalformedURLException( "Unsupported scheme: " + scheme);
		}
	}

	/**
	 * This method determines if this is a URI with a scheme of "rtsp" or "rtsps".
	 *
	 * @return true if the scheme is valid, false otherwise.
	 */
	// Overriding the URIImpl method
	public boolean isRtspURL()
	{
		return true;
	}

	/**
	 * Returns true if this SipURI is secure i.e. if this SipURI represents a
	 * sips URI. A sip URI returns false.
	 *
	 * @return  <code>true</code> if this SipURI represents a sips URI, and
	 * <code>false</code> if it represents a sip URI.
	 */
	public boolean isSecure()
	{
		return m_bSecure;
	}

	/**
	 * Sets the scheme of this URI to sip or sips depending on whether the
	 * argument is true or false. The default value is false.
	 *
	 * @param secure The boolean value indicating if the SipURI is secure.
	 */
	public void setSecure( boolean secure)
	{
		m_bSecure = secure;
		m_scheme = RTSPS;
	}

	/**
	 * Set the host part of this RtspURL to the newly supplied <code>host</code> parameter.
	 *
	 * @throws java.text.ParseException which signals that an error has been reached
	 * unexpectedly while parsing the host value.
	 */
	public void setHost( String host) throws MalformedURLException
	{
		m_host = host;
	}

	/**
	* Returns the host part of this RtspURL.
	*
	* @return The host part of this RtspURL
	*/
	public String getHost()
	{
		return m_host;
	}

	/**
	 * Set the port part of this RtspURL to the newly supplied port parameter.
	 *
	 * @param port The new interger value of the port of this RtspURL
	 */
	public void setPort( int port)
	{
		m_hostPort = port;
	}

	/**
	 * Returns the port part of this RtspURL.
	 *
	 * @return The port part of this RtspURL
	 */
	public int getPort()
	{
		return m_hostPort;
	}

	/**
	 * Set the host port of this RtspURL to the newly supplied <code>hostPort</code> parameter.
	 *
	 * @throws java.text.ParseException which signals that an error has been reached
	 * unexpectedly while parsing the host value.
	 */
	public void setHostPort( String host, int port) throws MalformedURLException
	{
		m_host = host;
        m_hostPort = port;
	}

	/**
	 * @param path Set the RTSP path
	 */
	public void setPath( String path)
	{
		m_path = path;
	}

	/**
	 * @return The RTSP path
	 */
	public String getPath()
	{
		return m_path;
	}

	/**
	 * Returns a string representation of this RTSP URL
	 *
	 * @return The string field representation of the RTSP URL
	 */
	public String encode()
	{
		StringBuilder result = new StringBuilder();
		result.append( m_scheme);
		result.append( "://").append( m_host).append(":").append(m_hostPort);
		if( m_path != null)
		{
			result.append("/");
            result.append( m_path);
		}
		return result.toString();
	}

	/**
	 * Returns a string representation of this RTSP URL
	 *
	 * @return The string field representation of the SIP URI
	 */
	@Override
	public String toString()
	{
		return encode();
	}

	/**
	 * Scheme definitions
	 */
	private static final String RTSP = "rtsp";
	private static final String RTSPU = "rtspu";
	private static final String RTSPS = "rtsps";

	/*
	 * Member variables
	 */
	private boolean m_bSecure;
	private String m_scheme;
    private String m_host;
	private int m_hostPort;
	private String m_path;

    private static final Logger log = LoggerFactory.getLogger(RtspURL.class);
}
