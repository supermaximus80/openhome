package com.icontrol.openhomesimulator.camera.resources;


import com.icontrol.openhome.data.*;
import com.icontrol.openhomesimulator.util.Utilities;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestClient;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.*;
import com.icontrol.rest.framework.service.Endpoint;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.icontrol.ohsimsolver.Wrappers;
import com.icontrol.ohsimsolver.ResponseStatusFactory;

import java.net.URL;
import java.util.Date;

@Resource("System")
public class SystemResource {

    protected static final Logger log = LoggerFactory.getLogger(SystemResource.class);

    @Resource("reboot")
    public static class RebootResource {

        @Endpoint
        public ResponseStatus get() throws Exception{
            return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_OPERATION);
        }

        @Endpoint
        public ResponseStatus post() throws Exception
        {
            log.debug("Received POST system/reboot");
            return ResponseStatusFactory.getResponseOK();
        }
    }

    @Resource("updateFirmware")
    public static class UpdateFirmwareResource {
        static Date lastUpdateTime = new Date();
        static URL firmwareUpdateURL = null;

        @Endpoint
        public ResponseStatus get() throws Exception{
            return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_OPERATION);
        }

        @Endpoint
        public ResponseStatus post(FirmwareDownload firmwareDownload) throws Exception{
            log.debug("Received POST system/updatefirmware. url="+firmwareDownload.getUrl().getValue());
            if (firmwareDownload==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            try {
                firmwareUpdateURL = new URL(firmwareDownload.getUrl().getValue()) ;
                lastUpdateTime = new Date();
                return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.REBOOT_REQUIRED);
            } catch (Exception ex) {
                return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_XML_CONTENT) ;
            }
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public FirmwareDownload get() {
                FirmwareDownload f = new FirmwareDownload();
                f.setUrl(Wrappers.createStringCap("https://firmware.icontrol.com/camera/x19992"));
                f.setFwVersion(Wrappers.createStringCap("1.1.20"));
                return f;
            }
        }

        @Resource("status")
        public static class StatusResource{
            @Endpoint
            public UpdateFirmwareStatus get() throws Exception{

                //log.debug("Received GET system/updatefirmware/status");
                UpdateFirmwareStatus s = new UpdateFirmwareStatus();
                s.setUpdateSuccess(Wrappers.createBooleanCap(true));
                // datetime
                s.setUpdateTime(Wrappers.createDateTimeCap(lastUpdateTime)) ;
                // url
                if (firmwareUpdateURL != null)
                    s.setUrl(Wrappers.createStringCap(firmwareUpdateURL.toString()));
                else
                    s.setUrl(Wrappers.createStringCap(""));
                s.setDownloadPercentage(Wrappers.createPercentageCap(100));
                return s;
            }
        }
    }

    @Resource("ConfigurationData")
    public static class ConfigurationDataResource {
        @Resource("configFile")
        public static class ConfigFileResource{
            static  String configFileString = ExampleResource.get();
            @Endpoint
            public ResponseStatus put(ConfigFile config) throws Exception{
                //log.debug("Received PUT system/configurationdata/configfile");
                if (config==null){
                    throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
                }
                try {
                    configFileString = RestClient.toString(config, RestConstants.ContentType.TEXT_XML);
                } catch (Exception ex) {
                    log.error("SystemConfigurationDataConfigFile caught "+ex);
                }
                return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.REBOOT_REQUIRED);
            }

            @Endpoint
            public String get() throws Exception{
                return configFileString;
            }

            @Resource("example")
            public static class ExampleResource{
                @Endpoint
                static public String get() {
                    ConfigFile f = new ConfigFile();
                    StringBuilder sb = new StringBuilder();
                    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
                    sb.append("<ConfigFile version=\"1.0\">");
                    try {
                        sb.append(Utilities.stripXmlHeader(RestClient.toString(TimersResource.configTimers, RestConstants.ContentType.TEXT_XML)));
                        sb.append(Utilities.stripXmlHeader(RestClient.toString(DeviceInfoResource.deviceInfo, RestConstants.ContentType.TEXT_XML)));
                        sb.append(Utilities.stripXmlHeader(RestClient.toString(XmppGatewayResource.xmppGateway, RestConstants.ContentType.TEXT_XML)));
                    } catch (Exception ex) {
                        log.error("SystemConfigurationDataConfigFile.create() caught "+ex);
                    }
                    sb.append("</ConfigFile>");
                    return sb.toString();
                }
            }
        }

        @Resource("Timers")
        public static class TimersResource {
            public static  ConfigTimers configTimers = ExampleResource.get();

            @Endpoint
            public ResponseStatus put(ConfigTimers config) throws Exception{
                if (config==null){
                    throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
                }
                configTimers = config;
                return ResponseStatusFactory.getResponseOK();
            }

            @Endpoint
            public ConfigTimers get() throws Exception{
                return configTimers;
            }

            @Resource("example")
            public static class ExampleResource{
                @Endpoint
                static public ConfigTimers get() {
                    ConfigTimers c = new ConfigTimers();
                    MediaTunnelReadyTimers t = new MediaTunnelReadyTimers();
                    t.setMaxWait(Wrappers.createIntegerCap(10*60*60*1000));       // 10 min
                    c.setMediaTunnelReadyTimers(t);
                    // mediaUploadTimers
                    MediaUploadTimers ut = new MediaUploadTimers();
                    ut.setMinWait(Wrappers.createIntegerCap(500));
                    ut.setMaxWait(Wrappers.createIntegerCap(5000));
                    ut.setStepsizeWait(Wrappers.createIntegerCap(1000));
                    ut.setRetries(Wrappers.createIntegerCap(5));
                    c.setMediaUploadTimers(ut);
                    return c;
                }
            }

            /*
               methods to get timers
            */
            static public long getMaxMediaTunnelReadyWait() {
                return configTimers.getMediaTunnelReadyTimers().getMaxWait().getValue().longValue();
            }

            static public long getUploadMinWait() {
                return configTimers.getMediaUploadTimers().getMinWait().getValue().longValue();
            }

            static public long getUploadMaxWait() {
                return configTimers.getMediaUploadTimers().getMaxWait().getValue().longValue();
            }

            static public long getUploadStepSize() {
                return configTimers.getMediaUploadTimers().getStepsizeWait().getValue().longValue();
            }

            static public long getUploadRetries() {
                return configTimers.getMediaUploadTimers().getRetries().getValue().longValue();
            }
        }

    }


    @Resource("factoryReset")
    public static class FactoryResetResource {
        @Endpoint
        public ResponseStatus get() throws Exception{
            return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_OPERATION);
        }

        @Endpoint
        public ResponseStatus post(
                @HttpParam(value = "mode", optional = true,
                        description = "mode"
                ) String mode
        ) throws Exception{
            return ResponseStatusFactory.getResponseOK();
        }
    }

    @Resource("deviceInfo")
    public static class DeviceInfoResource {
        public static  DeviceInfo deviceInfo = ExampleRessource.get();

        @Endpoint
        public ResponseStatus put(DeviceInfo info) throws Exception{
            if (info==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            deviceInfo = info;
            return ResponseStatusFactory.getResponseOK();
        }

        @Endpoint
        public DeviceInfo get() throws Exception{
            // add read only params
            // uuid
            UUIDCap uuid = new UUIDCap();
            uuid.setValue("f81d4fae-7dec-11d0-1111-00a0c91e6bf6");    // just a random fake uuid
            deviceInfo.setDeviceID(uuid);

            deviceInfo.setModel(Wrappers.createStringCap("Model 100"));
            deviceInfo.setSerialNumber(Wrappers.createStringCap("11223344556600"));
            MACCap mac = new MACCap();
            mac.setValue("00-B0-D0-86-BB-F7");      // random example
            deviceInfo.setMacAddress(mac);
            deviceInfo.setFirmwareVersion(Wrappers.createStringCap("1.0.0.0-Fake"));
            deviceInfo.setApiVersion(Wrappers.createStringCap("1.7"));
            return deviceInfo;
        }

        @Resource("example")
        public static class ExampleRessource{
            @Endpoint
            static public DeviceInfo get() {
                DeviceInfo d = new DeviceInfo();
                d.setDeviceName(Wrappers.createStringCap("iControlCameraSimulator"));
                return d;
            }
        }

    }

/*
    @Resource("inputs/privacy")
    public static class InputsPrivacyResource {
        static  InputPrivacy inputPrivacy = ExampleResource.get();

        @Endpoint
        public ResponseStatus put(InputPrivacy privacy) throws Exception{
            //log.debug("Received PUT system/inputs/privacy");
            if (privacy==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            inputPrivacy = privacy;
            return ResponseStatusFactory.getResponseOK();
        }

        @Endpoint
        public InputPrivacy get() throws Exception{
            //log.debug("Received GET system/inputs/privacy");
            return inputPrivacy;
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public InputPrivacy get() {
                InputPrivacy d = new InputPrivacy();
                d.setVideoInputPrivacy(OnOffCap.OFF);
                return d;
            }
        }


    }
*/

    @Resource("XMPP/gateway")
    public static class XmppGatewayResource {
        public static  XMPPGateway xmppGateway = ExampleResource.get();

        @Endpoint
        public ResponseStatus put(XMPPGateway gateway) throws Exception{
            //log.debug("Received PUT system/xmpp/gateway");
            if (gateway==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            xmppGateway = gateway;
            return ResponseStatusFactory.getResponseOK();
        }

        @Endpoint
        public XMPPGateway get() throws Exception{
            //log.debug("Received GET system/xmpp/gateway");
            return xmppGateway;
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public XMPPGateway get() {
                XMPPGateway d = new XMPPGateway();
                d.setEnabled(Wrappers.createBooleanCap(false));
                d.setHostName(Wrappers.createStringCap(""));
                return d;
            }

        }

    }

    @Resource("Host/server")
    public static class HostServerResource {
        static  HostServer httpServer = ExampleResource.get();

        @Endpoint
        public ResponseStatus put(HostServer server) throws Exception{
            //log.debug("Received PUT system/http/server");
            if (server==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            httpServer = server;
            return ResponseStatusFactory.getResponseOK();
        }

        @Endpoint
        public HostServer get() throws Exception{
            //log.debug("Received GET system/http/server");
            return httpServer;
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public HostServer get() {
                HostServer d = new HostServer();
                HostServerHttp http = new HostServerHttp();
                http.setEnabled(Wrappers.createBooleanCap(true));
                http.setPort(Wrappers.createIntegerCap(8080));      // TODO
                d.setHttp(http);
                HostServerHttp https = new HostServerHttp();
                https.setEnabled(Wrappers.createBooleanCap(false));
                d.setHttps(https);
                HostServerPoll poll = new HostServerPoll();
                poll.setEnabled(Wrappers.createBooleanCap(false));
                d.setPoll(poll);
                return d;
            }
        }
    }

    @Resource("Ping")
    public static class PingResource {
        @Endpoint
        public String get() throws Exception {
            return "OK";
        }
    }
}
