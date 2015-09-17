package com.icontrol.openhomesimulator.camera.resources;

import com.icontrol.openhome.data.*;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.icontrol.ohsimsolver.ResponseStatusFactory;
import com.icontrol.ohsimsolver.Wrappers;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.PathVar;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Resource("System/Video/inputs")
public class SystemVideoInputsResource {

    protected static final Logger log = LoggerFactory.getLogger(SystemVideoInputsResource.class);

    private static Map<String, VideoInputChannel> channelMap = new ConcurrentHashMap<String,VideoInputChannel>();

    static{
        VideoInputChannel channel = ChannelsResource.IdResource.ExampleResource.get();
        channelMap.put(channel.getId().getValue(),channel);
    }

    @Endpoint
    public VideoInput get() throws Exception {
        VideoInput input = new VideoInput();
        VideoInputChannelList list = new VideoInputChannelList();
        list.getVideoInputChannel().addAll(channelMap.values());
        input.getVideoInputChannelListAndExtensions().add(list);
        return input;
    }

    @Resource("channels")
    public static class ChannelsResource {

        @Endpoint
        public VideoInputChannelList get() throws Exception {
            VideoInputChannelList list = new VideoInputChannelList();
            list.getVideoInputChannel().addAll(channelMap.values());
            return list;
        }

        public static class ExampleResource{
            static public VideoInputChannelList get() {
                VideoInputChannelList channelList = new VideoInputChannelList();
                VideoInputChannel c = IdResource.ExampleResource.get();
                channelList.getVideoInputChannel().add(c);
                return channelList;
            }
        }


        @Resource("[UID]")
        public static class IdResource{
            @Endpoint
            public VideoInputChannel get(@PathVar("UID") String id) throws Exception {
                VideoInputChannel channel = channelMap.get(id);
                if (channel == null) {
                    throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                }
                return channel;
            }

            @Endpoint
            public ResponseStatus put(@PathVar("UID") String id, VideoInputChannel channel) throws Exception{
                //log.debug("Received "+pathPrefix+id);
                if (channel==null){
                    throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
                }
                channelMap.put(id, channel);
                return ResponseStatusFactory.getResponseOK();
            }

            @Resource("example")
            public static class ExampleResource{
                @Endpoint
                static public VideoInputChannel get(){
                    VideoInputChannel c = new VideoInputChannel();
                    c.setId(Wrappers.createIdCap("0"));
                    c.setBrightnessLevel(Wrappers.createPercentageCap(50));
                    c.setContrastLevel(Wrappers.createPercentageCap(50));
                    c.setSaturationLevel(Wrappers.createPercentageCap(50));
                    c.setSharpnessLevel(Wrappers.createPercentageCap(80));
                    // daynight filter
                    VideoDayNightFilter dayNightFilter = new VideoDayNightFilter();
                    VideoDayNightFilter.DayNightFilterType type = new VideoDayNightFilter.DayNightFilterType();
                    type.setValue("auto");
                    dayNightFilter.setDayNightFilterType(type);
                    dayNightFilter.setSwitchScheduleEnabled(Wrappers.createBooleanCap(false));
                    c.setDayNightFilter(dayNightFilter);
                    // rotation degree
                    VideoRotationDegree degree = new VideoRotationDegree();
                    degree.setValue(new BigInteger("0"));
                    c.setRotationDegree(degree);
                    // mirror enabled
                    c.setMirrorEnabled(Wrappers.createBooleanCap(false));

                    return c;
                }
            }

        }
    }

}
