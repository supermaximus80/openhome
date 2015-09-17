package com.icontrol.android.openhomesimulator.camera;

import com.icontrol.android.ohsimsolver.Wrappers;
import com.icontrol.android.openhomesimulator.util.AuthenticationInfo;
import com.icontrol.android.openhomesimulator.util.OpenHomeProperties;
import com.icontrol.android.openhomesimulator.util.URLConnectionHelper;
import com.icontrol.android.openhomesimulator.util.Utilities;
import com.icontrol.openhome.data.DateTimeCap;
import com.icontrol.openhome.data.MediaUploadFailure;
import com.icontrol.openhome.data.UploadType;
import com.icontrol.rest.framework.RestClient;
import com.icontrol.rest.framework.RestConstants;
import org.slf4j.Logger;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.*;

public class MediaUploader extends TimerTask  {

    AuthenticationInfo auth;
    SSLSocketFactory factory;
    Logger log;
    String id;
    String uploadUrl;
    String uploadFailureUrl;
    byte[] media;
    String contentType;

    public MediaUploader(AuthenticationInfo auth, SSLSocketFactory factory, Logger log) {
        this.auth = auth;
        this.factory = factory;
        this.log = log;
        this.id = null;
        this.uploadUrl = null;
        this.uploadFailureUrl = null;
        this.media = null;
        this.contentType = null;
    }

    public void start(String id, String uploadUrl, String uploadFailureUrl, byte[] media, String contentType) {
        uploadUrl=uploadUrl.contains("127.0.0.1:")?uploadUrl.replace("127.0.0.1:","10.0.2.2:"):uploadUrl;
        uploadUrl=uploadUrl.contains("localhost:")?uploadUrl.replace("localhost:","10.0.2.2:"):uploadUrl;
        uploadFailureUrl=uploadFailureUrl.contains("127.0.0.1:")?uploadFailureUrl.replace("127.0.0.1:","10.0.2.2:"):uploadFailureUrl;
        uploadFailureUrl=uploadFailureUrl.contains("localhost:")?uploadFailureUrl.replace("localhost:","10.0.2.2:"):uploadFailureUrl;

        this.id = id;
        this.uploadUrl = uploadUrl;
        this.uploadFailureUrl = uploadFailureUrl;
        this.media = media;
        this.contentType = contentType;

        // start upload task in a separate thread
        Timer execTimer = new Timer();
        execTimer.schedule(this, 100);

    }

    /*
        TimerTask run
     */
    public void run() {
        log.debug("PostStreamingChannelsPictureUpload.run id="+id+" uploadUrl="+uploadUrl);

        int code = 0;
        try {
            code = upload("POST", uploadUrl, media, contentType) ;
        } catch (Exception ex) {
            log.error("Upload failed. caught "+ex);
            code = -1;
        }

        if (code != 200 && uploadFailureUrl != null) {
            try {
                log.error("Upload unsuccessful code="+code+" Post to failure url = "+uploadFailureUrl);
                uploadFailure("POST", uploadFailureUrl, id, contentType) ;
            } catch (Exception ex) {
                log.error("uploadFailure caught "+ex);
            }
        }

    }

    public int upload(String method, String urlStr, byte[] media, String contentType) throws IOException {
        if (method==null || urlStr==null || media==null)
            throw new IOException("MediaUploader invalid paramter") ;

        HttpURLConnection conn = null;
        OutputStream os = null;

        log.debug("Uploading to url: " + urlStr);

        try {
            HashMap query = new HashMap();
            conn = new URLConnectionHelper(auth, factory).getConnection(urlStr, query);

            // set method
            conn.setRequestMethod(method);
            conn.setDoInput(true);

            String serialNo = OpenHomeProperties.getProperty("cameraSimulator.serialNo");
            boolean requireBasic = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.basic", false);
            boolean requireDigest = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.digest", false);
            if (serialNo != null && (requireBasic || requireDigest) && auth != null && auth.getChallengeResponse() != null) {
                log.debug("challenge response: " + auth.getChallengeResponse());
                conn.setRequestProperty("Authorization", auth.getChallengeResponse());
            }

            // set request headers
            if (method.equals("PUT") || method.equals("POST")) {
                conn.setRequestProperty("Content-Language", "en-US");
                conn.setRequestProperty("X-Capture-Time", Long.toString(System.currentTimeMillis()));

                conn.setUseCaches(false);
                conn.setDoOutput(true);

                // set headers
                conn.setRequestProperty("Content-Type", contentType);
                conn.setRequestProperty("Content-Length", "" + Integer.toString(media.length));

                os = conn.getOutputStream();
                os.write(media);
                os.flush();
                os.close();
            }

            Map headerFields = conn.getHeaderFields();
            Iterator iterator = headerFields.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry)iterator.next();
                log.debug("Received Header: " + entry.getKey() + ": " + entry.getValue());
            }

            // read response
            int code = conn.getResponseCode();
            log.debug("code: " + code);

            if (serialNo != null && auth != null && (requireBasic || requireDigest) && code == 401) {
                String authResponse = null;
                if (requireBasic) {
                    authResponse = Utilities.getBasicAuthHeader(auth.getUsername(), auth.getPassword());
                    auth.setChallengeResponse(authResponse);
                }

                if (requireDigest) {
                    try {
                        authResponse = Utilities.getDigestAuthHeader(auth.getUsername(), auth.getPassword(),
                                conn.getRequestMethod(), conn.getURL().getPath(), conn.getHeaderField("WWW-Authenticate"));
                        auth.setChallengeResponse(authResponse);
                    } catch (Exception e) {
                        throw new IOException("Failed to generate challenge response: " + e.getMessage());
                    }
                }

                log.debug("Handling automatic authentication challenge response for serialNo: " + serialNo + " for " + urlStr);
                conn = new URLConnectionHelper(auth, factory).getConnection(urlStr, query);

                // set method
                conn.setRequestMethod(method);
                conn.setDoInput(true);
                conn.setRequestProperty("Authorization", authResponse);

                // set request headers
                if (method.equals("PUT") || method.equals("POST")) {
                    conn.setRequestProperty("Content-Language", "en-US");
                    conn.setRequestProperty("X-Capture-Time", Long.toString(System.currentTimeMillis()));

                    conn.setUseCaches(false);
                    conn.setDoOutput(true);

                    // set headers
                    conn.setRequestProperty("Content-Type", contentType);
                    conn.setRequestProperty("Content-Length", "" + Integer.toString(media.length));
                    os = conn.getOutputStream();
                    os.write(media);
                    os.flush();
                    os.close();
                }

                // read response
                code = conn.getResponseCode();
                log.debug("Code again: " + code);
            }

            log.debug("Uploaded to url: " + urlStr + " - code: " + code) ;
            return code;
        } finally {
            if (os != null) {
                os.flush ();
                os.close ();
            }

            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Throwable t) {
                    log.error(t.getMessage(), t);
                }
            }
        }
    }

    public int uploadFailure(String method, String urlStr, String id, String uploadType) throws Exception {
        urlStr = urlStr.trim();
        if (urlStr.startsWith("xmpp://"))
            return uploadFailureXmpp(method, urlStr, id, uploadType);
        else
            return uploadFailureHttp(method, urlStr, id, uploadType);
    }

    public int uploadFailureXmpp(String method, String urlStr, String id, String uploadType) throws Exception {
        String xmlBody = genMediaUploadFailure(id, uploadType);
        //CameraSimulatorFactory.getInstance().sendXmppNotification(method, urlStr, xmlBody);
        return 200;
    }

    private String genMediaUploadFailure(String id, String uploadType) throws Exception {
        // create MediaUploadFailure XML object
        MediaUploadFailure failure = new MediaUploadFailure();
        // id
        failure.setId(Wrappers.createStringCap(id));
        // dateTime
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(new Date());
        XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        DateTimeCap dateTime = new DateTimeCap();
        dateTime.setValue(xgcal);
        failure.setDateTime(dateTime);
        //  uploadtype
        if (uploadType.startsWith("image"))
            failure.setUploadType(UploadType.PICTURE);
        else
            failure.setUploadType(UploadType.VIDEOCLIP);
        return RestClient.toString(failure, RestConstants.ContentType.TEXT_XML);
    }

    public int uploadFailureHttp(String method, String urlStr, String id, String uploadType) throws Exception {
        HttpURLConnection conn = null;
        DataOutputStream wr = null;
        try {
            String xmlBody = genMediaUploadFailure(id, uploadType);

            // http connection
            HashMap query = new HashMap();
            conn = new URLConnectionHelper(auth, factory).getConnection(urlStr, query);

            // set method
            conn.setRequestMethod(method);
            conn.setDoInput(true);

            String serialNo = OpenHomeProperties.getProperty("cameraSimulator.serialNo");
            boolean requireBasic = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.basic", false);
            boolean requireDigest = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.digest", false);
            if (serialNo != null && (requireBasic || requireDigest) && auth != null && auth.getChallengeResponse() != null) {
                log.debug("challenge response: " + auth.getChallengeResponse());
                conn.setRequestProperty("Authorization", auth.getChallengeResponse());
            }

            // set request headers
            if (method.equals("PUT") || method.equals("POST")) {
                conn.setRequestProperty("Content-Language", "en-US");

                conn.setUseCaches(false);
                conn.setDoOutput(true);

                //Send request
                if (xmlBody != null && xmlBody.length() > 0) {
                    conn.setRequestProperty("Content-Type", "application/xml");
                    conn.setRequestProperty("Content-Length", "" + Integer.toString(xmlBody.getBytes().length));
                    wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(xmlBody);

                    // add history event
                    History.getInstance().createNotifyEvent(urlStr, new Date(), null, xmlBody, 0);
                }
            }

            // read response
            int code = conn.getResponseCode();
            log.debug("code: " + code);

            if (serialNo != null && auth != null && (requireBasic || requireDigest) && code == 401) {
                String authResponse = null;
                if (requireBasic) {
                    authResponse = Utilities.getBasicAuthHeader(auth.getUsername(), auth.getPassword());
                    auth.setChallengeResponse(authResponse);
                }

                if (requireDigest) {
                    try {
                        authResponse = Utilities.getDigestAuthHeader(auth.getUsername(), auth.getPassword(),
                                conn.getRequestMethod(), conn.getURL().getPath(), conn.getHeaderField("WWW-Authenticate"));
                        auth.setChallengeResponse(authResponse);
                    } catch (Exception e) {
                        throw new IOException("Failed to generate challenge response: " + e.getMessage());
                    }
                }

                log.debug("Handling automatic basic authentication for serialNo: " + serialNo + " for " + urlStr);
                conn = new URLConnectionHelper(auth, factory).getConnection(urlStr, query);

                // set method
                conn.setRequestMethod(method);
                conn.setDoInput(true);
                conn.setRequestProperty("Authorization", authResponse);

                // set request headers
                if (method.equals("PUT") || method.equals("POST")) {
                    conn.setRequestProperty("Content-Language", "en-US");

                    conn.setUseCaches(false);
                    conn.setDoOutput(true);

                    //Send request
                    if (xmlBody != null && xmlBody.length() > 0) {
                        conn.setRequestProperty("Content-Type", "application/xml");
                        conn.setRequestProperty("Content-Length", "" + Integer.toString(xmlBody.getBytes().length));
                        wr = new DataOutputStream(conn.getOutputStream());
                        wr.writeBytes(xmlBody);

                        // add history event
                        History.getInstance().createNotifyEvent(urlStr, new Date(), null, xmlBody, 0);
                    }
                }

                // read response
                code = conn.getResponseCode();
                log.debug("Code again: " + code);
            }

            log.debug("Uploaded failure to url: " + urlStr + " - code: " + code);
            return code;
        } finally {
            if (wr != null) {
                wr.flush();
                wr.close();
            }

            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Throwable t) {
                    log.error(t.getMessage(), t);
                }
            }
        }
    }
}
