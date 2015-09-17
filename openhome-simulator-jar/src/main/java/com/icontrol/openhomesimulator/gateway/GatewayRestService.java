package com.icontrol.openhomesimulator.gateway;

import com.icontrol.openhomesimulator.gateway.resources.BootStrapResource;
import com.icontrol.openhomesimulator.gateway.resources.MediaPutGetResource;
import com.icontrol.openhomesimulator.gateway.resources.SimpleRelayResource;

import com.icontrol.rest.framework.RestService;


public class GatewayRestService extends RestService {

    public GatewayRestService() {
            super(null, new String[]{
                    BootStrapResource.class.getName(),
                    MediaPutGetResource.class.getName(),
                    SimpleRelayResource.class.getName(),

            },null);
    }

}
