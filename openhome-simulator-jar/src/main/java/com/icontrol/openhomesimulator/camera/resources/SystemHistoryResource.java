package com.icontrol.openhomesimulator.camera.resources;

import com.icontrol.openhome.data.*;
import com.icontrol.openhomesimulator.camera.History;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.icontrol.ohsimsolver.Wrappers;
import com.icontrol.ohsimsolver.ResponseStatusFactory;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.HttpParam;

@Resource("System/history")
public class SystemHistoryResource {

    protected static final Logger log = LoggerFactory.getLogger(SystemHistoryResource.class);
    private static HistoryConfiguration historyConfig = ConfigurationResource.ExampleResource.get();

    @Resource("configuration")
    public static class ConfigurationResource {

        @Endpoint
        public HistoryConfiguration get() throws Exception {
            return historyConfig;
        }

        @Endpoint
        public ResponseStatus put(HistoryConfiguration c) throws Exception
        {
            if (c==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            History.getInstance().setMaxCommandEntries(c.getCommandHistorySize().getValue().intValue());
            History.getInstance().setMaxNotificationEntries(c.getNotificationHistorySize().getValue().intValue());
            historyConfig = c;
            return ResponseStatusFactory.getResponseOK();
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public HistoryConfiguration get(){
                HistoryConfiguration c = new HistoryConfiguration();
                c.setCommandHistorySize(Wrappers.createIntegerCap(History.getInstance().getMaxCommandEntries()));
                c.setNotificationHistorySize(Wrappers.createIntegerCap(History.getInstance().getMaxNotificationEntries()));
                return c;
            }
        }
    }

    @Endpoint
    public String get(
            @HttpParam(value = "sinceCommand", optional = true,
                    description = "sinceCommand"
            ) Long sinceCommand,
            @HttpParam(value = "sinceNotification", optional = true,
                    description = "sinceNotification"
            ) Long sinceNotify
    ) throws Exception{
        return History.getInstance().getHistoryList(sinceCommand, sinceNotify) ;
    }
}
