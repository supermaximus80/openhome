package com.icontrol.openhomesimulator.gateway.resources;

import com.icontrol.openhome.data.CreateMediaTunnelFailure;
import com.icontrol.openhomesimulator.gateway.simplerelay.SimpleRelayManager;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.icontrol.rest.framework.RestfulResponse;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.PathVar;

import java.io.IOException;

public class SimpleRelayResource {

    protected static final Logger log = LoggerFactory.getLogger(SimpleRelayResource.class);

    @Resource("createmediatunnel/failed/[sessionID]")
    public static class CreateMediaTunnelFailedClass {
        private final static String pathName = "createmediatunnel/failed/";

        @Endpoint
        public void post(RestfulResponse res, @PathVar("sessionID") String sessionID, CreateMediaTunnelFailure failed) throws IOException, javax.xml.datatype.DatatypeConfigurationException {

            log.warn("Received CreateMediaTunnelFailed. id=" + failed.getId().getValue() + " dateTime=" + failed.getDateTime().getValue().toXMLFormat());
            if (sessionID == null || sessionID.length() < 1){
                throw new IOException("Invalid post CreateMediaTunnelFailed parameter");
            }
            SimpleRelayManager.getInstance().removeRelay(sessionID);
            res.setStatus(200);
        }
        static public String getPathName() {
            return pathName ;
        }
    }
}
