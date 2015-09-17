package com.icontrol.android.openhomesimulator.util;

import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class URLConnectionHelper {

    static {
        String maxResponseTimeoutStr = OpenHomeProperties.getProperty("timeout.response", "10");
        if (maxResponseTimeoutStr != null) {
            int maxResponseTimeout = Integer.parseInt(maxResponseTimeoutStr)*1000;
            CONNECT_TIMEOUT = maxResponseTimeout;
            READ_TIMEOUT = maxResponseTimeout;
        }
    }

    /*
        static variables and methods
     */
    static private int CONNECT_TIMEOUT = 10000;
    static private int READ_TIMEOUT = 10000;

    /*
        internal variables
     */
    AuthenticationInfo auth;
    SSLSocketFactory factory;

    public URLConnectionHelper(AuthenticationInfo auth, SSLSocketFactory factory) {
        this.auth = auth;
        this.factory = factory;
    }

    public HttpURLConnection getConnection(String surl, Map query) throws IOException {
        return getConnection(surl, query, null);
    }

    public HttpURLConnection getConnection(String surl, Map query, Map<String, String> headers) throws IOException {
        HttpURLConnection conn;
        URL url = getURL(surl, query);

        if (surl.startsWith("https://")) {
            HttpsURLConnection sconn = (HttpsURLConnection) url.openConnection();

            sconn.setSSLSocketFactory(factory);
            sconn.setHostnameVerifier(new StubVerifier());

            conn = sconn;
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }

        // set timeouts
        conn.setConnectTimeout(10000); // 10 seconds
        conn.setReadTimeout(READ_TIMEOUT); // 10 second read timeout

        // add basic headers
        conn.setRequestProperty("Host", url.getHost());
        conn.setRequestProperty("Accept-Language", "en-us");
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        Date d = new Date();
        conn.setRequestProperty("Date", format.format(d));

        if (this.auth!=null){
            String authInfo= auth.getUsername()+":"+auth.getPassword();
            conn.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64String(authInfo.getBytes()));
        }

        // add additional request headers specified in params
        if (headers != null) {
            for (Iterator<String> iter=headers.keySet().iterator(); iter.hasNext(); ) {
                String name = iter.next();
                conn.setRequestProperty(name, headers.get(name));
            }
        }

        return conn;
    }

    private URL getURL(String url, Map query) throws IOException {
        boolean firsttime = true;
        StringBuffer buf = new StringBuffer(url);

        if (query != null) {
            Iterator iter = query.keySet().iterator();

            while (iter.hasNext()) {
                String key = (String) iter.next();

                if (firsttime) {
                    buf.append("?");
                    firsttime = false;
                } else {
                    buf.append("&");
                }
                buf.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode((String) query.get(key), "UTF-8"));
            }
        }
        return new URL(buf.toString());
    }

    /*
    SSLContext getSSLContext() throws Exception {
        if (log.isDebugEnabled()) log.debug("getSSLContext 1 keystore type:" + KeyStore.getDefaultType());
        KeyStore ks = KeyStore.getInstance("JKS");

        String filename = getRelayCertStorePath();
        String pass = getRelayCertStorePassword();

        if (filename.startsWith("resources/")) {
            ks.load(RelayMgrInstance.class.getResourceAsStream(filename), pass.toCharArray());
        } else {
            File kf = new File(filename);
            ks.load(new FileInputStream(kf), pass.toCharArray());
        }

        if (log.isDebugEnabled())
            log.debug("getSSLContext algorithm type:" + KeyManagerFactory.getDefaultAlgorithm() + "/" + TrustManagerFactory.getDefaultAlgorithm());
        // KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        kmf.init(ks, pass.toCharArray());

        if (log.isDebugEnabled()) log.debug("got KeyManagerFactory");

        // TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");

        sslContext.init(kmf.getKeyManagers(), new TrustManager[]{new RelayX509TrustManager(ks)}, null);
        // sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }
    */

    class StubVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
