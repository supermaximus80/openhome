package com.icontrol.openhomesimulator.camera.xmppclient;

import org.slf4j.Logger;
import com.icontrol.openhomesimulator.util.AuthenticationInfo;
import com.icontrol.openhomesimulator.util.URLConnectionHelper;
import com.icontrol.openhomesimulator.util.Utilities;

import javax.net.ssl.SSLSocketFactory;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

public class LocalHttpRequest {
    String baseURL;
    AuthenticationInfo auth;
    SSLSocketFactory factory;
    Logger log;

    public LocalHttpRequest(String baseURL, AuthenticationInfo auth, SSLSocketFactory factory, Logger log) {
        this.baseURL = baseURL;
        this.auth = auth;
        this.factory = factory;
        this.log = log;
    }


    public OpenHomeResponseIQ process(OpenHomeRequestIQ reqIQ) {
        HttpURLConnection conn = null;
        DataOutputStream wr = null;
        OpenHomeResponseIQ responseIQ = null;
        int retCode = 500;
        String errorString=null;
        try {
            // Form complete URL
            String urlStr;
            if (reqIQ.getAction().startsWith("/"))
                urlStr = baseURL + reqIQ.getAction();
            else
                urlStr = baseURL + "/" + reqIQ.getAction();

            log.debug("LocalHttpRequest - sending request to url: "+urlStr);

            // http connection
            HashMap query = new HashMap();
            conn = new URLConnectionHelper(auth, factory).getConnection(urlStr, query, reqIQ.getHeaderMap());

            // set method
            conn.setRequestMethod(reqIQ.getMethod());
            conn.setDoInput(true);

            // set request headers
            if (reqIQ.getMethod().equalsIgnoreCase("PUT") || reqIQ.getMethod().equalsIgnoreCase("POST")) {
                conn.setUseCaches(false);
                conn.setDoOutput(true);

                //Send request
                String bodyText = reqIQ.getBodyText();
                if (bodyText != null && bodyText.length() > 0) {
                    conn.setRequestProperty("Content-Type", "application/xml");
                    conn.setRequestProperty("Content-Length", "" + Integer.toString(bodyText.getBytes().length));
                    wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(bodyText);
                }
            }

            // read response
            retCode = conn.getResponseCode();
            log.debug("LocalHttpRequest - returned HTTP code: " + retCode + " for: " + urlStr);

            String contentType = conn.getContentType();
            int contentLen = conn.getContentLength();
            InputStream is = null;
            if (retCode >= 400) {
                is = conn.getErrorStream();
            } else {
                is = conn.getInputStream();
            }

            if (contentType != null && contentType.startsWith("image")) {
                byte[] rxImage = Utilities.readRawBytes(is, contentLen);
                log.error("Received image response. len=" + rxImage.length + " bytes. But ignored for XMPP channel");
            } else if (contentType != null && contentType.startsWith("video")) {
                byte[] rxVideo = Utilities.readRawBytes(is, contentLen);
                log.error("Received video response. len=" + rxVideo.length + " bytes. But ignored for XMPP channel");
            } else {     // text or text/xml
                StringBuilder sb = Utilities.readTextBytes(is, contentLen);
                responseIQ = new OpenHomeResponseIQ();
                responseIQ.setCode(retCode);
                responseIQ.setContentType(contentType);
                responseIQ.setBodyText(sb.toString());
                responseIQ.setPacketID(reqIQ.getPacketID());
            }
        } catch (Exception ex) {
            errorString = ex.toString();
            log.error("LocalHttpRequest - caught "+ex);
        } finally {
            if (wr != null) {
                try {
                    wr.flush ();
                } catch (Exception ex) {}
                try {
                    wr.close ();
                } catch (Exception ex) {}
            }
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Throwable t) {
                    log.error(t.getMessage(), t);
                }
            }
        }

        // if null responseIQ, create an error response
        if (responseIQ == null) {
            log.error("LocalHttpRequest NULL respoonseIQ. Generate error response");
            responseIQ = new OpenHomeResponseIQ();
            responseIQ.setCode(retCode);
            responseIQ.setBodyText("LocalHttpRequest - caught "+errorString);
            responseIQ.setPacketID(reqIQ.getPacketID());
        }

        return responseIQ;
    }
}
