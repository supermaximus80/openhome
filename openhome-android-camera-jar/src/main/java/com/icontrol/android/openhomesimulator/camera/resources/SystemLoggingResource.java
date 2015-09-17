package com.icontrol.android.openhomesimulator.camera.resources;

import com.icontrol.android.ohsimsolver.ResponseStatusFactory;
import com.icontrol.android.ohsimsolver.Wrappers;
import com.icontrol.openhome.data.LoggingConfig;
import com.icontrol.openhome.data.ResponseStatus;
import com.icontrol.openhome.data.Severity;
import com.icontrol.openhome.data.SeverityCap;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.HttpParam;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Resource("system/logging")
public class SystemLoggingResource {

    protected static final Logger log = LoggerFactory.getLogger(SystemLoggingResource.class);
    private static LoggingConfig loggingConfig = ExampleResource.get();


    @Endpoint
    public LoggingConfig get() throws Exception {
        return loggingConfig;
    }

    @Endpoint
    public ResponseStatus put(LoggingConfig c) throws javax.xml.datatype.DatatypeConfigurationException{
        if (c==null){
            throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
        }
        loggingConfig = c;
        return ResponseStatusFactory.getResponseOK();
    }

    @Resource("example")
    public static class ExampleResource{
        @Endpoint
        static public LoggingConfig get(){
            LoggingConfig c = new LoggingConfig();
            // trigger
            LoggingConfig.LogTrigger trigger = new LoggingConfig.LogTrigger();
            SeverityCap severityCap = new SeverityCap();
            severityCap.setValue(Severity.DEBUG);
            trigger.setSeverity(severityCap);
            c.setLogTrigger(trigger);
            // local log
            LoggingConfig.LocalLog localLog = new LoggingConfig.LocalLog();
            localLog.setMaxEntries(Wrappers.createIntegerCap(100));
            c.setLocalLog(localLog);
            return c;
        }
    }

    @Resource("logdata")
    public static class LogDataResource {
        @Endpoint
        public String get(
                @HttpParam(value = "since", optional = true,
                        description = "since"
                ) Long since ) throws Exception
        {
            return "<logData>\t12/12/2011 Log line #1\n\t12/12/2011 Log line #2</logData>";
        }
    }
}
