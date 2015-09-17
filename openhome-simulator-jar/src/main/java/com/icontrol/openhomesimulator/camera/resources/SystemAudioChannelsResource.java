package com.icontrol.openhomesimulator.camera.resources;

import com.icontrol.openhome.data.*;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.*;
import com.icontrol.rest.framework.service.Endpoint;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.icontrol.ohsimsolver.ResponseStatusFactory;
import com.icontrol.ohsimsolver.Wrappers;

import java.util.HashMap;
import java.util.Map;


@Resource("System/Audio/channels/[UID]")
public class SystemAudioChannelsResource {

    protected static final Logger log = LoggerFactory.getLogger(SystemAudioChannelsResource.class);

    private static Map<String, AudioChannel> channelMap = new HashMap<String, AudioChannel>();

    //init
    static{
        AudioChannel audioChannel = ExampleResource.get();
        channelMap.put(audioChannel.getId().getValue(),audioChannel);
    }

    @Endpoint
    public AudioChannelList get() throws Exception {
        AudioChannelList list = new AudioChannelList();
        synchronized (channelMap) {
            list.getAudioChannel().addAll(channelMap.values());
        }
        return list;
    }

    @Endpoint
    public AudioChannel get(@PathVar("UID") String id) throws Exception {
        synchronized (channelMap) {
            AudioChannel channel = channelMap.get(id);
            if (channel == null) {
                throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
            }
            return channel;
        }
    }

    @Endpoint
    public ResponseStatus put(@PathVar("UID") String id, AudioChannel channel) throws Exception {
        if (channel==null){
            throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
        }
        synchronized (channelMap) {
            channelMap.put(id, channel);
        }
        return ResponseStatusFactory.getResponseOK();
    }

    @Resource("example")
    public static class ExampleResource{
        @Endpoint
        public static AudioChannel get(){
            AudioChannel c = new AudioChannel();
            c.setId(Wrappers.createIdCap("0"));
            c.setEnabled(Wrappers.createBooleanCap(true));
            AudioChannel.AudioMode mode = new AudioChannel.AudioMode();
            mode.setValue("listenonly");
            c.setAudioMode(mode);
            c.setMicrophoneEnabled(Wrappers.createBooleanCap(true));
            return c;
        }
    }
}
