package com.icontrol.openhomesimulator.camera.resources;

import com.icontrol.openhome.data.*;
import com.icontrol.openhomesimulator.camera.mediatunnel.MediaTunnelProc;
import com.icontrol.openhomesimulator.camera.mediatunnel.MediaTunnelStatusInterf;
import com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory;
import com.icontrol.openhomesimulator.gateway.resources.SimpleRelayResource;
import com.icontrol.openhomesimulator.gateway.simplerelay.SimpleRelayManager;
import com.icontrol.openhomesimulator.util.AuthenticationInfo;
import com.icontrol.openhomesimulator.util.Utilities;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.*;
import com.icontrol.rest.framework.service.Endpoint;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.icontrol.ohsimsolver.ResponseStatusFactory;
import com.icontrol.ohsimsolver.Wrappers;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Resource("Streaming/MediaTunnel")
public class StreamingMediaTunnelResource{

    protected static final Logger log = LoggerFactory.getLogger(StreamingMediaTunnelResource.class);
    private static Map<String, MediaTunnelProc> mediaTunnels = new ConcurrentHashMap<String, MediaTunnelProc>();

    //no init

    @Endpoint
    public MediaTunnelList get() throws Exception
    {
        MediaTunnelList list = new MediaTunnelList();
        Iterator<MediaTunnelProc> iter = mediaTunnels.values().iterator();
        while(iter.hasNext()) {
            MediaTunnelProc p = iter.next();
            list.getMediaTunnel().add(createMediaTunnel(p.getSessionID(), p.getTransportSecurity(), p.getElapsedTime()));
        }
        return list;
    }

    @Resource("[UID]/status")
    public static class StatusResource{
        @Endpoint
        public MediaTunnel get(@PathVar("UID") String id) throws Exception
        {
            MediaTunnelProc tunnelProc = mediaTunnels.get(id);
            if (tunnelProc != null) {
                MediaTunnel tunnel = createMediaTunnel(id, tunnelProc.getTransportSecurity(), tunnelProc.getElapsedTime());
                return tunnel;
            } else {
                throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
            }
        }
    }

    @Resource("create")
    public static class CreateResource implements MediaTunnelStatusInterf{
        @Endpoint
        public ResponseStatus get() throws Exception{
            return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_OPERATION);
        }

        @Endpoint
        public ResponseStatus post(CreateMediaTunnel createCmd) throws Exception{
            if (createCmd==null || createCmd.getSessionID()==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST,"");
            }
            String id = createCmd.getSessionID().getValue();
            if (mediaTunnels.get(id) != null) {
                ResponseStatus response = ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.DEVICE_BUSY) ;
                response.setStatusString("Session id already exists. id="+id);
                log.error("Unable to create media tunnel, session already exists. id="+id);
                return response;
            }

            // start tunnel creation
            AuthenticationInfo authenticationInfo = null;
            SSLSocketFactory factory = Utilities.getClientSideSSLSocketFactory();
            MediaTunnelProc p = new MediaTunnelProc(authenticationInfo, factory, log);

            try {
                ResponseStatusFactory.STATUSCODE code =  p.start(id,createCmd.getGatewayURL().getValue(),createCmd.getFailureURL().getValue(),this);
                ResponseStatus response = null;
                if (code == ResponseStatusFactory.STATUSCODE.OK) {
                    // add to map
                    mediaTunnels.put(id, p);
                    response = ResponseStatusFactory.getResponseOK();
                    response.setId(createCmd.getSessionID().getValue());
                } else {
                    response = ResponseStatusFactory.getResponseError(code);
                }
                return response;
            } catch (MalformedURLException ex) {
                log.error("post "+"streaming/mediatunnel/create"+" caught "+ex);
                return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_XML_CONTENT) ;
            } catch (IOException ex) {
                log.error("post "+"streaming/mediatunnel/create"+" caught "+ex);
                return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_XML_CONTENT) ;
            }
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public CreateMediaTunnel get() {
                CreateMediaTunnel c = new CreateMediaTunnel();
                String sessionID = SimpleRelayManager.getInstance().getCurSID();
                c.setSessionID(Wrappers.createStringCap(sessionID));
                c.setGatewayURL(Wrappers.createStringCap(SimpleRelayManager.getInstance().getCameraRelayURL(sessionID))) ;
                if (GatewaySimulatorFactory.getInstance().numXmppClientSessions() > 0)
                    c.setFailureURL(Wrappers.createStringCap("xmpp://" + GatewaySimulatorFactory.getInstance().getXmppServerDomainName() +"/" + SimpleRelayResource.CreateMediaTunnelFailedClass.getPathName() + sessionID));
                else
                    c.setFailureURL(Wrappers.createStringCap(GatewaySimulatorFactory.getInstance().getRelayPrefix() + SimpleRelayResource.CreateMediaTunnelFailedClass.getPathName() + sessionID));
                return c;
            }
        }

        @Override
        public void createdFailed(String id) {
            MediaTunnelProc p = mediaTunnels.get(id);
            if (p != null)
                mediaTunnels.remove(id) ;
        }
    }



    @Resource("[UID]/destroy")
    public static class DestroyResource {
        @Endpoint
        public ResponseStatus get() throws Exception{
            return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_OPERATION);
        }

        @Endpoint
        public ResponseStatus post(@PathVar("UID") String id) throws Exception
        {
            MediaTunnelProc p = mediaTunnels.get(id);
            if (p != null) {
                p.close();
                mediaTunnels.remove(id) ;
                return ResponseStatusFactory.getResponseOK();
            } else {
                throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
            }
        }
    }

    /*
    not used in camera simulator for webapp
     */
    @Resource("http/mjpg")
    public static class HttpMjpgResource{
        @Endpoint
        public ResponseStatus get() throws Exception{
            //not valid for camera simulator. only for android openhome camera
            return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_OPERATION);
        }
    }

    static private MediaTunnel createMediaTunnel(String id, String security, int elapsedTime) {
        MediaTunnel tunnel = new MediaTunnel();
        tunnel.setSessionID(Wrappers.createStringCap(id));
        MediaTunnelSecurity s = new MediaTunnelSecurity();
        s.setValue(security);
        tunnel.setTransportSecurity(s);
        tunnel.setElapsedTime(Wrappers.createIntegerCap(elapsedTime));
        return tunnel;
    }
}
