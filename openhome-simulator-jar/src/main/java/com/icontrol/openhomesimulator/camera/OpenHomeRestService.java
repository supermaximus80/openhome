package com.icontrol.openhomesimulator.camera;

import com.icontrol.openhomesimulator.camera.resources.*;
import com.icontrol.rest.framework.ApiResource;
import com.icontrol.rest.framework.RestService;

public class OpenHomeRestService extends RestService {

    public OpenHomeRestService() {
        super(null, new String[]{
                SystemTimeResource.class.getName(),
                StreamingResource.class.getName(),
                StreamingMediaTunnelResource.class.getName(),
                SystemResource.class.getName(),
                SystemNetworkInterfacesResource.class.getName(),
                SystemVideoInputsResource.class.getName(),
                SystemAudioChannelsResource.class.getName(),
                SecurityResource.class.getName(),
                SystemLoggingResource.class.getName(),
                SystemHistoryResource.class.getName(),
                CustomEventResource.class.getName(),
//                CustomMotionDetectionVideoResource.class.getName(),
                CustomMotionDetectionPirResource.class.getName(),
                ApiResource.class.getName()
        },null);
    }
}