package com.icontrol.android.openhomesimulator.camera.resources;


import com.icontrol.android.ohsimsolver.ResponseStatusFactory;
import com.icontrol.android.ohsimsolver.Wrappers;
import com.icontrol.android.openhomesimulator.camera.CameraSimulator;
import com.icontrol.android.openhomesimulator.camera.CameraSimulatorFactory;
import com.icontrol.android.openhomesimulator.camera.MediaResourceLoader;
import com.icontrol.android.openhomesimulator.camera.MediaUploader;
import com.icontrol.android.openhomesimulator.util.AuthenticationInfo;
import com.icontrol.android.openhomesimulator.util.OpenHomeProperties;
import com.icontrol.android.openhomesimulator.util.Utilities;
import com.icontrol.openhome.data.*;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.PathVar;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Resource("streaming")
public class StreamingResource {

    protected static final Logger log = LoggerFactory.getLogger(StreamingResource.class);

    @Resource("status")
    public static class StatusResource {
        @Endpoint
        public StreamingStatus get() throws Exception {
            StreamingStatus status = (new StreamingStatus());
            IntegerCap intcap = (new IntegerCap());
            intcap.setValue(new BigInteger("0"));
            status.setTotalStreamingSessions(intcap);
            return status;
        }
    }

    @Resource("channels")
    public static class ChannelsResource {
        private static Map<String, StreamingChannel> channelMap = new HashMap<String, StreamingChannel>();

        static {
            List<StreamingChannel> list = createChannelList().getStreamingChannel();
            Iterator<StreamingChannel> iter = list.iterator();
            int i = 0;
            while (iter.hasNext()) {
                StreamingChannel channel = iter.next();
                channelMap.put(String.valueOf(i++), channel);
            }
        }

        public static StreamingChannel createChannel(){
            StreamingChannel channel = new StreamingChannel();
            StreamingChannel.Transport transport = new StreamingChannel.Transport();
            channel.setTransport(transport);
            return channel;
        }

        public static StreamingChannelList createChannelList(){
            StreamingChannelList streamingChannelList = new StreamingChannelList();
            StreamingChannel channel = createChannel();
            channel.setId(Wrappers.createIdCap("0"));
            channel.setEnabled(Wrappers.createBooleanCap(true));
            streamingChannelList.getStreamingChannel().add(channel);
            channel =createChannel();
            channel.setId(Wrappers.createIdCap("1"));
            channel.setEnabled(Wrappers.createBooleanCap(true));
            streamingChannelList.getStreamingChannel().add(channel);
            return streamingChannelList;
        }


        @Resource("example")
        public static class ExampleResource {
            @Endpoint
            public StreamingChannelList get() throws Exception{
                return createChannelList();
            }

        }

        @Endpoint
        public StreamingChannelList get() throws Exception {
            synchronized (channelMap){
                StreamingChannelList channelList = new StreamingChannelList();
                channelList.getStreamingChannel().addAll(channelMap.values());
                return channelList;
            }
        }

        @Endpoint
        public ResponseStatus put(StreamingChannelList channelList) throws Exception {
            synchronized (channelMap){
                channelMap.clear();
                List<StreamingChannel> list = channelList.getStreamingChannel();
                Iterator<StreamingChannel> iter = list.iterator();
                int i = 0;
                while (iter.hasNext()) {
                    StreamingChannel channel = iter.next();
                    channelMap.put(String.valueOf(i++), channel);
                }
                return ResponseStatusFactory.getResponseOK();
            }
        }

        @Endpoint
        public ResponseStatus delete() throws Exception {
            synchronized (channelMap){
                channelMap.clear();
                return ResponseStatusFactory.getResponseOK();
            }
        }

        @Resource("[id]")
        public static class IdResource{
            @Endpoint
            public StreamingChannel get(@PathVar("id") String id) throws Exception {
                StreamingChannel channel;
                synchronized (channelMap){
                    channel = channelMap.get(id);
                }
                if (channel == null) {
                    throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                }
                return channel;
            }

            @Endpoint
            public ResponseStatus put(@PathVar("id") String id, StreamingChannel channel) throws Exception {
                synchronized (channelMap){
                    channelMap.put(id, channel);
                }
                return ResponseStatusFactory.getResponseOK();
            }

            @Endpoint
            public ResponseStatus delete(@PathVar("id") String id) throws Exception {
                synchronized (channelMap){
                    channelMap.remove(id);
                }
                return ResponseStatusFactory.getResponseOK();
            }
            @Resource("example")
            public static class ExampleResource {
                @Endpoint
                public StreamingChannel get(@PathVar("id") String id) throws Exception {
                    return createChannel();
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
                public ResponseStatus post(MediaUpload upload)  throws Exception
                {
                    String id = upload.getId().getValue().trim();
                    String type = upload.getSnapShotImageType().getValue().value().trim();
                    String uploadUrl = upload.getGatewayUrl().getValue().trim();
                    String uploadFailureUrl = upload.getFailureUrl().getValue().trim();

                    if (uploadUrl==null || uploadUrl.length() < 1)  {
                        log.error("Invalid parameter");
                        return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_XML_CONTENT);
                    }

                    byte [] image=MediaResourceLoader.loadImageFromAndroid();

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
            }

            @Resource("video/upload")
            public static class VideoUploadResource {
                @Endpoint
                public ResponseStatus get() throws Exception{
                    return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_OPERATION);
                }

                @Endpoint
                public ResponseStatus post(MediaUpload upload)  throws Exception{
                    String id = upload.getId().getValue();
                    MediaUpload.VideoClipFormatType type = upload.getVideoClipFormatType();
                    String uploadUrl = upload.getGatewayUrl().getValue();
                    String uploadFailureUrl = upload.getFailureUrl().getValue();

                    if (uploadUrl==null || uploadUrl.length() < 1)  {
                        log.error("Invalid parameter");
                        return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_XML_CONTENT);
                    }

                    //byte[] video = MediaResourceLoader.loadVideo(640, 480, type);
                    byte [] video = MediaResourceLoader.loadVideoFromAndroid();

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
                        System.out.println(video.length);
                        // start upload task in a separate thread
                        new MediaUploader(authenticationInfo, factory, log).start(id, uploadUrl, uploadFailureUrl, video, "video/mp4");
                        return ResponseStatusFactory.getResponseOK();
                    } catch (Exception e) {
                        log.error("Failed to upload video: " + e.getMessage());

                        return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.DEVICE_ERROR);
                    }
                }
            }
        }
    }
}

