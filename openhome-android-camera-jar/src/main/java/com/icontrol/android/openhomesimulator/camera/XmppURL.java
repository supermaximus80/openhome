/*
 */
package com.icontrol.android.openhomesimulator.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

public class XmppURL
{
	/**
	 * Constructor
	 *
	 */
	public XmppURL()
	{
		m_scheme = XMPP;
        m_host = null;
		m_hostPort = 0;
		m_path = null;
	}

    /*
        parse XMPP url
        example:  XMPP://127.0.0.1:8080/notify/path
     */
    public XmppURL(String urlStr) throws MalformedURLException
    {
        if (urlStr==null || urlStr.length() == 0)
            throw new MalformedURLException("Invalid urlstr="+urlStr) ;
        int pos = urlStr.indexOf("://");
        if (pos == -1)
            throw new MalformedURLException("Invalid urlstr="+urlStr) ;
        m_scheme = urlStr.substring(0, pos);
        if (!XMPP.equalsIgnoreCase(m_scheme))
            throw new MalformedURLException("Invalid urlstr="+urlStr) ;

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
            m_hostPort = 5222;
            pos2 = urlStr.indexOf("/", pos) ;
            if (pos2 == -1)
                throw new MalformedURLException("Invalid urlstr="+urlStr) ;
            m_host = urlStr.substring(pos, pos2);
        }

        // get path
        pos = pos2 + 1;
        m_path = urlStr.substring(pos);

        //log.debug("XMPPURLstr="+urlStr+" decodedURL="+toString());
    }

	/**
	 * Override URIImpl methods
	 */

	/**
	 * Returns the value of the "scheme" of this URI, for example "XMPP", "XMPPu" or "XMPPs".
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
		if( scheme.equals( XMPP))
		{
			m_scheme = XMPP;
		}
		else
		{
			throw new MalformedURLException( "Unsupported scheme: " + scheme);
		}
	}

	/**
	 * This method determines if this is a URI with a scheme of "XMPP" or "XMPPs".
	 *
	 * @return true if the scheme is valid, false otherwise.
	 */
	// Overriding the URIImpl method
	public boolean isXMPPURL()
	{
		return true;
	}

	/**
	 * Set the host part of this XMPPURL to the newly supplied <code>host</code> parameter.
	 *
	 * @throws java.text.ParseException which signals that an error has been reached
	 * unexpectedly while parsing the host value.
	 */
	public void setHost( String host) throws MalformedURLException
	{
		m_host = host;
	}

	/**
	* Returns the host part of this XMPPURL.
	*
	* @return The host part of this XMPPURL
	*/
	public String getHost()
	{
		return m_host;
	}

	/**
	 * Set the port part of this XMPPURL to the newly supplied port parameter.
	 *
	 * @param port The new interger value of the port of this XMPPURL
	 */
	public void setPort( int port)
	{
		m_hostPort = port;
	}

	/**
	 * Returns the port part of this XMPPURL.
	 *
	 * @return The port part of this XMPPURL
	 */
	public int getPort()
	{
		return m_hostPort;
	}

	/**
	 * Set the host port of this XMPPURL to the newly supplied <code>hostPort</code> parameter.
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
	 * @param path Set the XMPP path
	 */
	public void setPath( String path)
	{
		m_path = path;
	}

	/**
	 * @return The XMPP path
	 */
	public String getPath()
	{
		return m_path;
	}

	/**
	 * Returns a string representation of this XMPP URL
	 *
	 * @return The string field representation of the XMPP URL
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

	@Override
	public String toString()
	{
		return encode();
	}

	/**
	 * Scheme definitions
	 */
	private static final String XMPP = "xmpp";

	/*
	 * Member variables
	 */
	private String m_scheme;
    private String m_host;
	private int m_hostPort;
	private String m_path;

    private static final Logger log = LoggerFactory.getLogger(XmppURL.class);
}
