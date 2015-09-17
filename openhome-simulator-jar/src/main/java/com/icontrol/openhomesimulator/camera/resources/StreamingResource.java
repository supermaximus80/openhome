package com.icontrol.openhomesimulator.camera.resources;


import com.icontrol.openhome.data.*;
import com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory;
import com.icontrol.openhomesimulator.gateway.resources.MediaPutGetResource;
import com.icontrol.openhomesimulator.camera.CameraSimulator;
import com.icontrol.openhomesimulator.camera.CameraSimulatorFactory;
import com.icontrol.openhomesimulator.camera.MediaResourceLoader;
import com.icontrol.openhomesimulator.camera.MediaUploader;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.*;
import com.icontrol.rest.framework.service.Endpoint;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.icontrol.openhomesimulator.util.AuthenticationInfo;
import com.icontrol.openhomesimulator.util.OpenHomeProperties;
import com.icontrol.openhomesimulator.util.Utilities;
import com.icontrol.ohsimsolver.Wrappers;
import com.icontrol.ohsimsolver.ResponseStatusFactory;


import javax.net.ssl.SSLSocketFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Resource("Streaming")
public class StreamingResource {

    protected static final Logger log = LoggerFactory.getLogger(StreamingResource.class);
    private static Map<String, StreamingChannel> mStreamingChannelMap = new HashMap<String, StreamingChannel>();

    static{
        StreamingChannel channel = ChannelsResource.IdResource.ExampleResource.get();
        mStreamingChannelMap.put(channel.getId().getValue(), channel);
    }


    @Resource("status")
    public static class StatusResource {
        @Endpoint
        public StreamingStatus get() throws Exception {
            StreamingStatus status = new StreamingStatus();
            status.setTotalStreamingSessions(Wrappers.createIntegerCap(0));
            return status;
        }
    }

    @Resource("channels")
    public static class ChannelsResource {

        @Resource("example")
        public static class ExampleResource {
            @Endpoint
            public static StreamingChannelList get(){
                StreamingChannelList streamingChannelList = new StreamingChannelList();
                StreamingChannel channel = IdResource.ExampleResource.get();
                streamingChannelList.getStreamingChannel().add(channel);
                return streamingChannelList;
            }
        }

        @Endpoint
        public StreamingChannelList get() throws Exception {
            synchronized (mStreamingChannelMap){
                StreamingChannelList channelList = new StreamingChannelList();
                channelList.getStreamingChannel().addAll(mStreamingChannelMap.values());
                return channelList;
            }
        }

        @Endpoint
        public ResponseStatus put(StreamingChannelList channelList) throws Exception {
            if (channelList==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            synchronized (mStreamingChannelMap){
                mStreamingChannelMap.clear();
                List<StreamingChannel> list = channelList.getStreamingChannel();
                Iterator<StreamingChannel> iter = list.iterator();
                int i = 0;
                while (iter.hasNext()) {
                    StreamingChannel channel = iter.next();
                    mStreamingChannelMap.put(String.valueOf(i++), channel);
                }
                return ResponseStatusFactory.getResponseOK();
            }
        }

        @Endpoint
        public ResponseStatus delete() throws Exception {
            synchronized (mStreamingChannelMap){
                mStreamingChannelMap.clear();
                return ResponseStatusFactory.getResponseOK();
            }
        }

        @Resource("[UID]")
        public static class IdResource{
            @Endpoint
            public StreamingChannel get(@PathVar("UID") String id) throws Exception {
                StreamingChannel channel;
                synchronized (mStreamingChannelMap){
                    channel = mStreamingChannelMap.get(id);
                }
                if (channel == null) {
                    throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                }
                return channel;
            }

            @Endpoint
            public ResponseStatus put(@PathVar("UID") String id, StreamingChannel channel) throws Exception {
                if (channel==null){
                    throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
                }
                synchronized (mStreamingChannelMap){
                    mStreamingChannelMap.put(id, channel);
                }
                return ResponseStatusFactory.getResponseOK();
            }

            @Endpoint
            public ResponseStatus delete(@PathVar("UID") String id) throws Exception {
                synchronized (mStreamingChannelMap){
                    mStreamingChannelMap.remove(id);
                }
                return ResponseStatusFactory.getResponseOK();
            }

            @Resource("example")
            public static class ExampleResource {
                @Endpoint
                public static StreamingChannel get(){
                    StreamingChannel channel = new StreamingChannel();
                    StreamTransport transport = new StreamTransport();
                    channel.setTransport(transport);
                    channel.setId(Wrappers.createIdCap("0"));
                    return channel;
                }
            }

            @Resource("status")
            public static class StatusResource {
                @Endpoint
                public StreamingSessionStatusList get() throws Exception {
                    StreamingSessionStatusList statusList = new StreamingSessionStatusList();
                    return statusList;
                }
            }


            @Resource("picture/upload")
            public static class PictureUploadResource {
                @Endpoint
                public ResponseStatus get() throws Exception{
                    return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_OPERATION);
                }

                @Endpoint
                public ResponseStatus post(MediaUpload upload)  throws Exception{
                    if (upload==null || upload.getId()==null){
                        throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
                    }
                    String id = upload.getId().getValue().trim();
                    String type = upload.getSnapShotImageType().getValue().value().trim();
                    String uploadUrl = upload.getGatewayUrl().getValue().trim();
                    String uploadFailureUrl = upload.getEventUrl().getValue().trim();

                    if (uploadUrl==null || uploadUrl.length() < 1)  {
                        log.error("Invalid parameter");
                        return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_XML_CONTENT);
                    }

                    byte[] image = MediaResourceLoader.loadImage(640, 480);
                    AuthenticationInfo authenticationInfo = null;

                    String serialNo = OpenHomeProperties.getProperty("cameraSimulator.serialNo");
                    boolean requireBasic = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.basic", false);
                    boolean requireDigest = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.digest", false);

                    if (serialNo != null && (requireBasic || requireDigest)) {
                        log.debug("Getting auth info for preset serialNo: " + serialNo);
                        CameraSimulator cameraSimulator = CameraSimulatorFactory.getInstance().getCameraInstance(serialNo);
                        authenticationInfo = cameraSimulator.getAuthenticationInfo();
                    }

                    try {
                        SSLSocketFactory factory = Utilities.getClientSideSSLSocketFactory();
                        // start upload task in a separate thread
                        new MediaUploader(authenticationInfo, factory, log).start(id, uploadUrl, uploadFailureUrl, image, "image/jpeg");
                        return ResponseStatusFactory.getResponseOK();
                    } catch (Exception e) {
                        log.error("Failed to upload picture: " + e.getMessage());

                        return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.DEVICE_ERROR);
                    }
                }

                @Resource("example")
                public static class ExampleResource {
                    @Endpoint
                    public static MediaUpload get() {
                        MediaUpload m = new MediaUpload();
                        // id
                        IdCap idCap = new IdCap();
                        idCap.setValue(Long.toString(System.currentTimeMillis()));
                        m.setId(idCap);
                        // snapshotImageType
                        ImageTypeCap imageTypeCap = new ImageTypeCap();
                        imageTypeCap.setValue(ImageType.JPEG);
                        m.setSnapShotImageType(imageTypeCap);
                        // gateway url
                        StringCap targetURL = new StringCap() ;
                        String targetURLstr =GatewaySimulatorFactory.getInstance().getMediaUploadURLPrefix()+ MediaPutGetResource.ImageResource.getPathName() + idCap.getValue();
                        targetURL.setValue(targetURLstr);
                        m.setGatewayUrl(targetURL);
                        // failure url
                        StringCap failureURL = new StringCap() ;
                        failureURL.setValue(GatewaySimulatorFactory.getInstance().getMediaUploadURLPrefix()+ MediaPutGetResource.FailedResource.getPathName() + idCap.getValue());
                        m.setEventUrl(failureURL);
                        return m;
                    }
                }
            }

            @Resource("video/upload")
            public static class VideoUploadResource {
                @Endpoint
                public ResponseStatus get() throws Exception{
                    return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_OPERATION);
                }

                @Endpoint
                public ResponseStatus post(MediaUpload upload)  throws Exception{
                    if (upload==null || upload.getId()==null){
                        throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
                    }
                    String id = upload.getId().getValue();
                    VideoClipFormatType type = upload.getVideoClipFormatType();
                    String uploadUrl = upload.getGatewayUrl().getValue();
                    String uploadFailureUrl = upload.getEventUrl().getValue();

                    if (uploadUrl==null || uploadUrl.length() < 1)  {
                        log.error("Invalid parameter");
                        return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_XML_CONTENT);
                    }

                    byte[] video =MediaResourceLoader.loadTestVideo();// MediaResourceLoader.loadVideo(640, 480, type);

                    AuthenticationInfo authenticationInfo = null;
                    String serialNo = OpenHomeProperties.getProperty("cameraSimulator.serialNo");
                    boolean requireBasic = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.basic", false);
                    boolean requireDigest = OpenHomeProperties.getProperty("authenticationSupport.camera-originated.digest", false);

                    if (serialNo != null && (requireBasic || requireDigest)) {
                        log.debug("Getting auth info for preset serialNo: " + serialNo);

                        CameraSimulator cameraSimulator = CameraSimulatorFactory.getInstance().getCameraInstance(serialNo);
                        authenticationInfo = cameraSimulator.getAuthenticationInfo();
                    }

                    try {
                        SSLSocketFactory factory = Utilities.getClientSideSSLSocketFactory();

                        // start upload task in a separate thread
                        new MediaUploader(authenticationInfo, factory, log).start(id, uploadUrl, uploadFailureUrl, video, "video/mp4");
                        return ResponseStatusFactory.getResponseOK();
                    } catch (Exception e) {
                        log.error("Failed to upload video: " + e.getMessage());

                        return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.DEVICE_ERROR);
                    }
                }

                @Resource("example")
                public static class ExampleResource {
                    @Endpoint
                    public MediaUpload get() throws Exception{
                        MediaUpload m = new MediaUpload();
                        // id
                        IdCap idCap = new IdCap();
                        idCap.setValue(Long.toString(System.currentTimeMillis()));
                        m.setId(idCap);
                        // videoClipformatType
                        VideoClipFormatType formatType = new VideoClipFormatType();
                        formatType.setValue("MP4");
                        m.setVideoClipFormatType(formatType);
                        // gateway url
                        StringCap targetURL = new StringCap() ;
                        String targetURLstr = GatewaySimulatorFactory.getInstance().getMediaUploadURLPrefix()+ MediaPutGetResource.VideoResource.getPathName() + idCap.getValue();
                        targetURL.setValue(targetURLstr);
                        m.setGatewayUrl(targetURL);
                        // failure url
                        StringCap failureURL = new StringCap() ;
                        if (GatewaySimulatorFactory.getInstance().numXmppClientSessions() > 0)
                            failureURL.setValue("xmpp://" + GatewaySimulatorFactory.getInstance().getXmppServerDomainName() +"/"+ MediaPutGetResource.FailedResource.getPathName() + idCap.getValue());
                        else
                            failureURL.setValue(GatewaySimulatorFactory.getInstance().getMediaUploadURLPrefix()+ MediaPutGetResource.FailedResource.getPathName() + idCap.getValue());
                        m.setEventUrl(failureURL);
                        return m;
                    }
                }
            }
        }
    }
}

