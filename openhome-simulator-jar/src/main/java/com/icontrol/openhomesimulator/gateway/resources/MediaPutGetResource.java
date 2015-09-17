package com.icontrol.openhomesimulator.gateway.resources;

import com.icontrol.rest.framework.RestfulResponse;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.PathVar;
import com.icontrol.openhome.data.*;
import com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory;
import com.icontrol.openhomesimulator.gateway.MediaInstance;
import com.icontrol.openhomesimulator.util.Utilities;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Resource("upload")
public class MediaPutGetResource {

    protected static final Logger log = LoggerFactory.getLogger(MediaPutGetResource.class);

    @Resource("image/[tsID]")
    public static class ImageResource {
        private final static String pathName = "upload/image/";

        @Endpoint
        public void post(HttpServletRequest req, RestfulResponse res, @PathVar("tsID") String tsID) throws Exception {

            if (tsID == null || tsID.length() < 1)
                throw new IOException("Invalid PostImageUpload parameter");

            /* check mandatory headers */
            if (200 != checkMandatoryHeaders(req)) {
                res.setStatus(400);
                return;
            }

            byte[] rxMedia = null;
            InputStream is = req.getInputStream();
            String contentType = req.getContentType();
            int contentLen = req.getContentLength();
            if (contentType != null && contentType.startsWith("image")) {
                rxMedia = Utilities.readRawBytes(is, contentLen);
                if (rxMedia != null && rxMedia.length > 0) {
                    log.debug("Received uploaded media len="+rxMedia.length+" bytes");
                    GatewaySimulatorFactory.getInstance().storeMedia(tsID, rxMedia, contentType);
                } else {
                    log.debug("Received upload POST but no data received");
                    res.setStatus(400);
                }
            } else {
                log.error("PostImageUpload contentType:"+contentType+" not compatible with image upload.");
                res.setStatus(400);
            }
        }

        @Endpoint
        public void get(RestfulResponse res, @PathVar("tsID") String tsID) throws Exception {
            if (tsID == null || tsID.length() < 1)
                throw new IOException("Invalid GET ImageResource parameter");

            int status = 200;
            OutputStream os = null;
            try {
                // load image
                MediaInstance mediaInst = GatewaySimulatorFactory.getInstance().getMedia(tsID);
                if (mediaInst == null)  {
                    status = 404;
                    return;
                }
                byte[] image = mediaInst.getMedia();
                // write data
                if (image != null && image.length > 0) {
                    res.setContentType("image/jpeg");
                    res.setContentLength(image.length);
                    res.setHeader("Cache-Control", "no-cache");
                    res.setHeader("Pragma", "no-cache");
                    res.setHeader("Expires", "Mon, 06 Jan 1990 00:00:01 GMT");

                    os = res.getOutputStream();
                    os.write(image);
                    os.flush();
                } else {
                    status = 404;
                }
            } catch (IOException ex) {
                log.error("Failed to upload image", ex);
                status = 404;
            } finally {
                if (os != null)
                    os.close();
            }

            if (status != 200)
                res.setStatus(status);
        }

        static public String getPathName() {
            return pathName;
        }
    }

    @Resource("video/[tsID]")
    public static class VideoResource {
        private final static String pathName = "upload/video/";

        @Endpoint
        public void post(HttpServletRequest req, RestfulResponse res, @PathVar("tsID") String tsID) throws Exception {

            if (tsID == null || tsID.length() < 1)
                throw new IOException("Invalid VideoResource parameter");

            /* check mandatory headers */
            if (200 != checkMandatoryHeaders(req)) {
                res.setStatus(400);
                return;
            }

            byte[] rxMedia = null;
            InputStream is = req.getInputStream();
            String contentType = req.getContentType();
            int contentLen = req.getContentLength();
            if (contentType != null && contentType.startsWith("video")) {
                rxMedia = Utilities.readRawBytes(is, contentLen);
                if (rxMedia != null && rxMedia.length > 0) {
                    log.debug("Received uploaded media len="+rxMedia.length+" bytes");
                    GatewaySimulatorFactory.getInstance().storeMedia(tsID, rxMedia, contentType);
                } else {
                    log.debug("Received upload POST but no data received");
                    res.setStatus(400);
                }
            } else {
                log.error("VideoResource contentType:"+contentType+" not compatible with video upload.");
                res.setStatus(400);
            }
        }

        @Endpoint
        public void get(RestfulResponse res, @PathVar("tsID") String tsID) throws Exception {
            if (tsID == null || tsID.length() < 1)
                throw new IOException("Invalid GET VideoResource parameter");

            int status = 200;
            OutputStream os = null;
            try {
                // load video
                MediaInstance mediaInst = GatewaySimulatorFactory.getInstance().getMedia(tsID);
                if (mediaInst == null)  {
                    status = 404;
                    return;
                }
                byte[] video = mediaInst.getMedia();
                // write data
                if (video != null && video.length > 0) {
                    res.setContentType("video/mp4");
                    res.setContentLength(video.length);
                    res.setHeader("Cache-Control", "no-cache");
                    res.setHeader("Pragma", "no-cache");
                    res.setHeader("Expires", "Mon, 06 Jan 1990 00:00:01 GMT");

                    os = res.getOutputStream();
                    os.write(video);
                    os.flush();
                } else {
                    status = 404;
                }
            } catch (IOException ex) {
                log.error("Failed to upload video", ex);
                status = 404;
            } finally {
                if (os != null)
                    os.close();
            }

            if (status != 200)
                res.setStatus(status);
        }

        static public String getPathName() {
            return pathName;
        }
    }


    @Resource("failed/[tsID]")
    public static class FailedResource {
        private final static String pathName = "upload/failed/";
        private final static String resourceName = "POST "+ pathName+"[tsID]";

        @Endpoint
        public void post(HttpServletRequest req, RestfulResponse res, @PathVar("tsID") String tsID, MediaUploadEvent failed) throws Exception {

            if (tsID == null || tsID.length() < 1)
                throw new IOException("Invalid post FailedResource parameter");
            log.warn("Received MediaUploadFailure. id=" + failed.getId().getValue() + " uploadType=" + failed.getUploadType() + " dateTime=" + failed.getDateTime().getValue().toXMLFormat());
            // inform user
            GatewaySimulatorFactory.getInstance().storeNotification(tsID, "MediaUpload Failed. type="+failed.getUploadType().name(), "alert");
        }

        static public String getPathName() {
            return pathName ;
        }
    }

    @Resource("eventalert")
    public static class EventAlertResource {
        private final static String pathName = "upload/eventalert/";

        @Endpoint
        public void post(EventAlert eventAlert) throws Exception {

            log.warn("Received EventAlert. " + " id=" + eventAlert.getId().getValue() + " time=" + eventAlert.getDateTime().getValue().toXMLFormat() + " state="+eventAlert.getEventState().getValue());
            // inform user
            GatewaySimulatorFactory.getInstance().storeNotification("eventAlert"+eventAlert.getId().getValue(), "MotionDetectionAlert. state="+eventAlert.getEventState().getValue(), "alert");
        }

        static public String getPathName() {
            return pathName ;
        }
    }

    /*
        Utility routine used to check for mandatory headers in incoming requests
     */
    private static int checkMandatoryHeaders(HttpServletRequest req) {

        String[] requiredHeaders = {"X-Capture-Time", "Host", "Content-Length", "Content-Type", "Date"};    // TODO add digest authorization

        for (int i=0; i<requiredHeaders.length;i++) {
            String header = req.getHeader(requiredHeaders[i]) ;
            if (header==null) {
                log.error("Reject upload post due to missing "+requiredHeaders[i]+" request header");
                return 400;
            }
        }
        return 200;
    }
}
