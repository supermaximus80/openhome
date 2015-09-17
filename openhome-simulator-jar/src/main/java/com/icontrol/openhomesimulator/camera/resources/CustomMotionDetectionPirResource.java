package com.icontrol.openhomesimulator.camera.resources;

import com.icontrol.ohsimsolver.Wrappers;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.*;
import com.icontrol.rest.framework.RestfulResponse;
import com.icontrol.ohsimsolver.ResponseStatusFactory;

import com.icontrol.openhome.data.*;
import com.icontrol.rest.framework.service.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Resource("Event/MotionDetection/PIR")
public class CustomMotionDetectionPirResource {

    protected static final Logger log = LoggerFactory.getLogger(CustomMotionDetectionPirResource.class);
    private static final int PIR_VALID_TIME_DEFAULT = 200;  // 200 ms
    private static final int PIR_MOTION_TIME_DEFAULT = 3000;  // 3 sec

    private static Map<String, MotionDetection> motionPIRMap = null;

    //init
    static {
        try {
            motionPIRMap = new HashMap<String, MotionDetection>();
            MotionDetection m = IdResource.ExampleResource.get();
            motionPIRMap.put(m.getId().getValue(), m);
        } catch (Exception e) {
            log.error("CustomMotionDetectionPirResource init caught "+e);
        }
    }

    @Endpoint
    static public MotionDetectionList get() throws Exception {
        MotionDetectionList list = new MotionDetectionList();
        synchronized (motionPIRMap){
            list.getMotionDetection().addAll(motionPIRMap.values());
        }
        return list;
    }

    public static class ExampleResource{
        static public MotionDetectionList get(){
            MotionDetectionList list = new MotionDetectionList();
            MotionDetection m = IdResource.ExampleResource.get();
            list.getMotionDetection().add(m);
            return list;
        }
    }

    @Resource("[UID]")
    public static class IdResource{
        @Endpoint
        public MotionDetection get(@PathVar("UID") String id) throws Exception {
            synchronized (motionPIRMap){
                MotionDetection m = motionPIRMap.get(id);
                if (m == null) {
                    throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                }
                return m;
            }
        }

        @Endpoint
        public ResponseStatus put(@PathVar("UID") String id, MotionDetection m) throws javax.xml.datatype.DatatypeConfigurationException
        {
            if (m==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST,"");
            }
            synchronized (motionPIRMap){
                motionPIRMap.put(id, m);
            }
            return ResponseStatusFactory.getResponseOK();
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public MotionDetection get(){
                MotionDetection m = new MotionDetection();
                m.setId(Wrappers.createIdCap("0"));
                m.setEnabled(Wrappers.createBooleanCap(true));
                
                MotionPIR pir = new MotionPIR();
                pir.setPirMotionTime(Wrappers.createIntegerCap(PIR_MOTION_TIME_DEFAULT));
                pir.setPirValidTime(Wrappers.createIntegerCap(PIR_VALID_TIME_DEFAULT));
                
                m.setPIR(pir);
                
                return m;
            }
        }

    }
}
