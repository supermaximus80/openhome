package com.icontrol.android.openhomesimulator.camera.resources;

import com.icontrol.android.ohsimsolver.ResponseStatusFactory;
import com.icontrol.android.ohsimsolver.Wrappers;
import com.icontrol.android.openhomesimulator.camera.History;
import com.icontrol.openhome.data.HistoryConfiguration;
import com.icontrol.openhome.data.ResponseStatus;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.HttpParam;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Resource("system/history")
public class SystemHistoryResource {

    protected static final Logger log = LoggerFactory.getLogger(SystemHistoryResource.class);
    private static HistoryConfiguration historyConfig = null;
    static{

        historyConfig = createHistoryConfigurationExample();
    }

    static HistoryConfiguration createHistoryConfigurationExample(){
        HistoryConfiguration c = new HistoryConfiguration();
        c.setCommandHistorySize(Wrappers.createIntegerCap(History.getInstance().getMaxCommandEntries()));
        c.setNotificationHistorySize(Wrappers.createIntegerCap(History.getInstance().getMaxNotificationEntries()));
        return c;
    }

    @Resource("configuration")
    public static class ConfigurationResource {

        @Endpoint
        public HistoryConfiguration get() throws Exception {
            return historyConfig;
        }

        @Endpoint
        public ResponseStatus put(HistoryConfiguration c) throws Exception
        {
            //log.debug("Received PUT "+path +" maxCommand="+c.getCommandHistorySize().getValue().intValue()+" maxNotify="+c.getNotificationHistorySize().getValue().intValue());
            // set history config params
            History.getInstance().setMaxCommandEntries(c.getCommandHistorySize().getValue().intValue());
            History.getInstance().setMaxNotificationEntries(c.getNotificationHistorySize().getValue().intValue());
            historyConfig = c;
            return ResponseStatusFactory.getResponseOK();
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public HistoryConfiguration get() throws Exception {
                return createHistoryConfigurationExample();
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
    ) throws Exception
    {
        return History.getInstance().getHistoryList(sinceCommand, sinceNotify) ;
    }
}
