package com.icontrol.android.openhomesimulator.camera.resources;

import com.icontrol.android.ohsimsolver.ResponseStatusFactory;
import com.icontrol.android.ohsimsolver.Wrappers;
import com.icontrol.openhome.data.AudioChannel;
import com.icontrol.openhome.data.AudioChannelList;
import com.icontrol.openhome.data.ResponseStatus;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.PathVar;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


@Resource("system/audio/channels/[id]")
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
    public AudioChannel get(@PathVar("id") String id) throws Exception {
        synchronized (channelMap) {
            AudioChannel channel = channelMap.get(id);
            if (channel == null) {
                throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
            }
            return channel;
        }
    }

    @Endpoint
    public ResponseStatus put(@PathVar("id") String id, AudioChannel channel) throws Exception {
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
