package com.icontrol.android.openhomesimulator.camera.resources;

import com.icontrol.android.ohsimsolver.ResponseStatusFactory;
import com.icontrol.android.ohsimsolver.Wrappers;
import com.icontrol.openhome.data.MotionDetection;
import com.icontrol.openhome.data.MotionDetectionList;
import com.icontrol.openhome.data.ResponseStatus;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.RestfulResponse;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.PathVar;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Resource("custom/motiondetection/pir")
public class CustomMotionDetectionPirResource {

    protected static final Logger log = LoggerFactory.getLogger(CustomMotionDetectionPirResource.class);
    private static final int PIR_VALID_TIME_DEFAULT = 200;  // 200 ms
    private static final int PIR_MOTION_TIME_DEFAULT = 3000;  // 3 sec

    private static Map<String, MotionDetection> motionPIRMap = null;

    @Endpoint
    static public MotionDetectionList get() throws Exception {
        MotionDetectionList list = new MotionDetectionList();
        synchronized (motionPIRMap){
            list.getMotionDetection().addAll(motionPIRMap.values());
        }
        return list;
    }

    @Resource("[id]")
    public static class IdResource{
        @Endpoint
        public MotionDetection get(@PathVar("id") String id) throws Exception {
            synchronized (motionPIRMap){
                MotionDetection m = motionPIRMap.get(id);
                if (m == null) {
                    throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                }
                return m;
            }
        }

        @Endpoint
        public ResponseStatus put(@PathVar("id") String id, MotionDetection m) throws javax.xml.datatype.DatatypeConfigurationException
        {
            if (m==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST,"");
            }
            synchronized (motionPIRMap){
                motionPIRMap.put(id, m);
            }
            return ResponseStatusFactory.getResponseOK();
        }

    }
}
