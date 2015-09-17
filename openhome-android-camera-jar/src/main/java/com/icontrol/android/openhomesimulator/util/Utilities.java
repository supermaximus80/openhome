package com.icontrol.android.openhomesimulator.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.net.ssl.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;


public class Utilities {

    private static final Logger log = LoggerFactory.getLogger(Utilities.class);

    public static String[] splitToArray(CharSequence src, char delim) {
        ArrayList list = new ArrayList();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == delim && sb.length() > 0) {
                list.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }
        String x[] = new String[list.size()];
        for (int i = 0; i < x.length; i++) {
            x[i] = (String) list.get(i);
        }
        return x;
    }

    /**
     * need to replace
     * - carriage return (CR, ASCII 0x0D)
     * - line feed (LF, ASCII 0x0A)
     * unless immediately followed by space or newline
     * see RFC2616
     *
     * @param value
     * @return
     */
    public static String headerEncode(String value) {
        // return value.replace("\r", "%0D").replace("\n", "%0A");
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(value, "\r\n", true);
        LinkedList<String> tokens = new LinkedList<String>();
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        while (!tokens.isEmpty()) {
            String next = tokens.pop();
            if (next.equals("\r") || next.equals("\n")) {
                String following = "";
                if (!tokens.isEmpty()) {
                    following = tokens.pop();
                }
                if (following.startsWith(" ") || following.startsWith("\t")) {
                    sb.append(next);
                } else {
                    if (next.equals("\r")) {
                        sb.append("%0D");
                    } else if (next.equals("\n")) {
                        sb.append("%0A");
                    }
                }
                tokens.push(following);
            } else {
                sb.append(next);
            }
        }
        return sb.toString();
    }

    public static byte[] readRawBytes(InputStream is, int len) throws IOException {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(len);

            byte buf[] = new byte[1024];
            int count;
            int cursize = 0;

            while (cursize < len && (count = is.read(buf)) >= 0) {
                if (count > 0) {
                    os.write(buf, 0, count);
                }
                cursize += count;
            }
            os.close();
            if (cursize != len) {
                throw new IOException("Incomplete data received. contentlen=" + len + " but received " + cursize);
            }
            return os.toByteArray();
        } catch (Exception ex) {
            throw new IOException(ex);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Throwable t) {

                }
            }
        }
    }

    public static StringBuilder readTextBytes(InputStream is, int len) throws IOException {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb;
        } catch (Exception ex) {
            throw new IOException(ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable t) {

                }
            }
        }
    }

    public static void addLineFeed(StringBuilder sb) {
        String str = sb.toString();
        str = str.replace("><", ">\r\n<");
        sb.delete(0, sb.length());
        sb.append(str);
    }

    /*
       getQueryMap
     */
    public static Map<String, String> getQueryMap(String query) {
        Map<String, String> map = new HashMap<String, String>();
        if (query == null)
            return map;

        String[] params = query.split("&");
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    /*
        get local ip address
     */
    public static String getLocalIPaddress() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (Exception ex) {
            return "localhost";
        }
    }

    /**
     * Returns SSLSocketFactory for client side (ie, camera simulator and gateway simulator) based on client truststore
     *
     * @return
     * @throws Exception
     */

    private static Object monitor = new Object();
    private static SSLSocketFactory clientSSLSocketFactoryInst = null;

    public static SSLSocketFactory getClientSideSSLSocketFactory() throws Exception {
        synchronized (monitor) {
            if (clientSSLSocketFactoryInst == null)
                clientSSLSocketFactoryInst = getClientSideSSLSocketFactory_internal();
        }
        return clientSSLSocketFactoryInst;
    }

    private static SSLSocketFactory getClientSideSSLSocketFactory_internal() throws Exception {
        String keyPassword = "changeit";
        KeyStore keyStore = KeyStore.getInstance("bks");
        keyStore.load(Utilities.class.getResourceAsStream("/openHomeClientTruststore.bks"), keyPassword.toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
        keyManagerFactory.init(keyStore, keyPassword.toCharArray());

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new SecureRandom());
        return context.getSocketFactory();
    }

    /**
     * Returns SSLServerSocket intended to be used on a server-side (ie, relay server)
     *
     * @return
     * @throws Exception
     */
    public static SSLServerSocket getSSLServerSocket() throws Exception {
        String keyPassword = "changeit";
        KeyStore keyStore = KeyStore.getInstance("bks");
        keyStore.load(Utilities.class.getResourceAsStream("/openHomeTomcatKeystore.bks"), keyPassword.toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
        keyManagerFactory.init(keyStore, keyPassword.toCharArray());

        // TrustManager code copied from relayserver
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), null);

        return (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket();
    }

    // Used to replace "https" vs "http" protocol strings
    public static String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    /**
     * Returns Digest Authentication Header over HTTP
     *
     * @param username
     * @param password
     * @param httpMethod
     * @param httpPath
     * @param httpAuthHeader
     * @return
     * @throws Exception
     */
    public static String getDigestAuthHeader(String username, String password,
                                             String httpMethod, String httpPath, String httpAuthHeader) throws Exception {
        // HA1 = MD5(username:realm:password)
        // HA2 = MD5(httpMethod:httpPath)
        // Digest response = MD5(HA1:nonce:nc:cnonce:qop:HA2)

        log.debug("username = " + username);
        log.debug("password = " + password);
        log.debug("httpMethod = " + httpMethod);
        log.debug("httpPath = " + httpPath);
        log.debug("httpAuthHeader = " + httpAuthHeader);

        HeaderElement[] elements = HeaderElement.parse(httpAuthHeader);
        Map map = new HashMap();
        for (int i = 0; i < elements.length; i++) {
            map.put(elements[i].getName(), elements[i].getValue());
        }

        String realm = (String) map.get("Digest realm");
        String qop = (String) map.get("qop");
        String nonce = (String) map.get("nonce");
        String opaque = (String) map.get("opaque");

        String nc = "1";
        String cnonce = String.valueOf(Math.abs(new Random().nextInt()));

        byte[] ha1Str = (username + ":" + realm + ":" + password).getBytes();

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(ha1Str);
        String ha1Digest = new String(Hex.encodeHex(md.digest()));

        byte[] ha2Str = (httpMethod + ":" + httpPath).getBytes();
        md.reset();
        md.update(ha2Str);
        String ha2Digest = new String(Hex.encodeHex(md.digest()));

        byte[] responseStr = (ha1Digest + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2Digest).getBytes();
        md.reset();
        md.update(responseStr);
        String responseDigest = new String(Hex.encodeHex(md.digest()));

        String authHeader = "Digest username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + httpPath + "\", qop="
                + qop + ", nc=" + nc + ", cnonce=\"" + cnonce + "\", response=\"" + responseDigest + "\", opaque=\"" + opaque + "\"";

        log.debug("authHeader: " + authHeader);
        return authHeader;
    }

    /**
     * Returns Basic Authication Header over HTTP
     * @param username
     * @param password
     * @return
     */
    public static String getBasicAuthHeader(String username, String password) {
        String authStr = username.trim() + ":" + (password != null ? password.trim() : "");
        return "Basic " + new String(Base64.encodeBase64(authStr.getBytes()));
    }

    /**
     * Pretty format XML - currently for UI usage only
     * //@param xml
     * @return
     */
    /*
    public static String prettyFormat(final String xml) {
        log.debug("before pretty xml: " + xml);
        String s = null;
        try {
            final OutputFormat format = OutputFormat.createPrettyPrint();
            final org.dom4j.Document document = DocumentHelper.parseText(xml);
            final StringWriter sw = new StringWriter();
            final XMLWriter writer = new XMLWriter(sw, format);
            writer.write(document);
            s = sw.toString();
        } catch (Exception e) {
            s = xml;
        }
        log.debug("after pretty xml: " + s);

        return s;
    }
    */


    /*
        strip off <?xml version="1.0" encoding="UTF-8" standalone="yes"?> from beg of string

     */
    public static String stripXmlHeader(String input) {
        int pos = input.indexOf("<?xml");
        if (pos == -1)
            return input;
        int pos2 = input.indexOf("?>",pos+2);
        if (pos == -1)
            return input;
        return input.substring(pos2+2);
    }
    /*
       create Document from reader in order to generate object from xml. Used in Gateway Simulator, Camera Simulator
    */

    static public Document getDocument(Reader source) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        for (String featureToFalse : new String[] {
                "http://xml.org/sax/features/validation",
                "http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
                XMLConstants.FEATURE_SECURE_PROCESSING,
                "http://apache.org/xml/features/nonvalidating/load-external-dtd"
        }) {
            try {
                documentBuilderFactory.setFeature(featureToFalse, false);
            } catch (ParserConfigurationException e) {
                // ignore
            }
        }
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                // if (systemId.equals("ALLOWED_DTD.dtd")) {
                // }
                return null;
            }
        });
        InputSource inputSource=new InputSource(source);
        Document document = builder.parse(inputSource);
        return document;
    }

    public static String generateStandardXML(String xml){

        xml=xml.replaceAll("\r","").replaceAll("\n","");
        xml=xml.substring(0,xml.indexOf(">"))+xml.substring(xml.indexOf(">")).replace(" ","");
        return xml;
    }

    public static String removeUnusedElementXML(String startTag, String endingTag, String xml){
        int start=xml.indexOf(startTag);
        int end=xml.lastIndexOf(endingTag);
        xml=xml.substring(0,start)+xml.substring(end+endingTag.length(), xml.length());
        return xml;
    }


    public static String convertStreamToString( InputStream is, String ecoding ) throws IOException
    {
        StringBuilder sb = new StringBuilder( Math.max( 16, is.available() ) );
        char[] tmp = new char[ 4096 ];

        try {
            InputStreamReader reader = new InputStreamReader( is, ecoding );
            for( int cnt; ( cnt = reader.read( tmp ) ) > 0; )
                sb.append( tmp, 0, cnt );
        } finally {
            is.close();
        }
        return sb.toString();
    }

}