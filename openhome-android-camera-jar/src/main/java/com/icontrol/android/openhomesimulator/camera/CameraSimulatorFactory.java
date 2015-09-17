package com.icontrol.android.openhomesimulator.camera;

import com.icontrol.android.openhomesimulator.util.OpenHomeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;


public class CameraSimulatorFactory {

    /*
        static variables and methods
     */
    private static final Logger log = LoggerFactory.getLogger(CameraSimulatorFactory.class);

    static Object monitor = new Object();

    static private CameraSimulatorFactory simulator = null;

    static public CameraSimulatorFactory getInstance() {
        synchronized(monitor) {
            if (simulator == null) {
                simulator = new CameraSimulatorFactory();
            }
        }
        return simulator;
    }

    static public CameraSimulatorFactory createInstance() throws IOException {
        synchronized(monitor) {
            if (simulator == null)
                simulator = new CameraSimulatorFactory();
        }
        return simulator;
    }

    static final String rtspSourceURL_local_vga = "rtsp://192.168.2.50/img/media2.sav";
    static final String rtspSourceURL_local_qvga = "rtsp://192.168.2.50/img/media.sav";
    static final String mjpegSourceURL_local_vga = null;

    /*
        class variables and methods
     */
    HashMap<String, CameraSimulator> cameraMap;
    HashMap<String, RtspURL> rtspSourceUrlMap;
    HashMap<String, URL> mjpegSourceUrlMap;
    HashMap<String, String> sizeToChannelIdMap;

    String contextPath;
    boolean bEnableXmppMode;

    private OpenHomeRestService restServiceInst = null;
    private String lastRegistryGwURL = OpenHomeProperties.getProperty("cameraSimulator.registryGatewayURL", null);
    private String lastSerialNo = OpenHomeProperties.getProperty("cameraSimulator.serialNo", "112233445566");

    private CameraSimulatorFactory() {
        cameraMap = new HashMap<String, CameraSimulator>();
        // rtsp source url
        restServiceInst = new OpenHomeRestService();
        log.debug("openhomeRestImpl constructed successfully.");
        rtspSourceUrlMap = new HashMap<String, RtspURL>();
        try {
            rtspSourceUrlMap.put("640x480", new RtspURL(rtspSourceURL_local_vga)) ;
            rtspSourceUrlMap.put("320x240", new RtspURL(rtspSourceURL_local_qvga)) ;
        } catch (MalformedURLException e) {
            log.error("CameraSimulatorFactory constructor caught "+e);
        }
        // mjpeg source url
        mjpegSourceUrlMap = new HashMap<String, URL>();
        try {
            if (mjpegSourceURL_local_vga != null)
                mjpegSourceUrlMap.put("640x480", new URL(mjpegSourceURL_local_vga)) ;
        } catch (MalformedURLException e) {
            log.error("CameraSimulatorFactory constructor caught "+e);
        }
        // size to channel ID
        sizeToChannelIdMap = new HashMap<String, String>();
        sizeToChannelIdMap.put("320x240","0");
        sizeToChannelIdMap.put("640x480","1");

        // context path
        contextPath = null; // TODO, retrieve this from Tomcat
        //bEnableXmppMode=false;
        bEnableXmppMode = OpenHomeProperties.getProperty("gatewaySimulator.enableXmpp", false);
    }

    /*
        getCameraInstance
     */
    public CameraSimulator getCameraInstance(String serialNo) throws IOException {
        if (serialNo==null)
            throw new IOException("Invalid serialNo");

        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera==null)
            throw new IOException("Unable to find camera for serialNo "+serialNo);

        return camera;
    }

    /*
        get first instance in the map
     */
    public CameraSimulator getCameraInstance() throws IOException {
        if (cameraMap == null)
            return null;
        Iterator<CameraSimulator> iter = cameraMap.values().iterator();
        if (!iter.hasNext())
            return null;

        CameraSimulator camera = iter.next();
        if (camera==null)
            throw new IOException("Unable to find any camera");

        return camera;
    }

    /*
        Destroy, called upon servlet destroy
     */
    public void destroy() {
        Iterator<CameraSimulator> iter = cameraMap.values().iterator() ;
        while(iter.hasNext()) {
            iter.next().destroy();
        }
    }

    /*

     */

    public OpenHomeRestService getRestServiceInst() {
        return restServiceInst;
    }

    public void setContextPath(String path) throws IOException {
        /*
        String protocol = request.getRequestURL().toString().toLowerCase();
        int pos = protocol.indexOf(":") ;
        if (pos > 0)
            protocol = protocol.substring(0, pos) ;
        else
            protocol = "https";
        contextPath = protocol +"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath(); */
        contextPath=path;
    }

    public String getOpenHomeApiPath() {
        return contextPath ;
    }

    /*
        createCamera, deleteCamera
     */
    public CameraSimulator createCamera(String serialNo, String siteID, String sharedSecret, String xmppHost, int xmppPort) throws Exception {
        // retrieve existing CameraSimcamera, if not, create new one
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null) {
            camera = new CameraSimulator(serialNo, siteID, sharedSecret, xmppHost, xmppPort, null);
            cameraMap.put(serialNo, camera);
        }
        return camera;
    }

    public void deleteCamera(String serialNo) {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera != null) {
            camera.destroy();
            cameraMap.remove(serialNo);
        }
    }

    /*
        startBootStrap
     */
    public int startBootStrap(String registryGwURL, String serialNo, String activationKey, Logger log) throws Exception  {
        lastRegistryGwURL = registryGwURL;
        lastSerialNo = serialNo;

        // retrieve existing CameraSimcamera, if not, create new one
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null) {
            camera = new CameraSimulator(serialNo, log);
            cameraMap.put(serialNo, camera);
        }

        return camera.startBootStrap(registryGwURL, serialNo, activationKey);
    }

    public String getSiteID(String serialNo) {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null)
            return "";
        return camera.getSiteID();
    }

    public String getSharedSecret(String serialNo) throws Exception {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (bEnableXmppMode) {
            if (camera == null)
                return "";
        } else {
            // retrieve existing CameraSimcamera, if not, create new one (use in OpenHome simulator mode, could be first instantiation of camera)
            if (camera == null) {
                camera = new CameraSimulator(serialNo, log);
                cameraMap.put(serialNo, camera);
            }
        }
        return camera.getSharedSecret();
    }

    public void setSharedSecret(String serialNo, String secret) throws Exception {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (bEnableXmppMode) {
            if (camera == null)
                throw new IOException("Camera not found");
        } else {
            // retrieve existing CameraSimcamera, if not, create new one (use in OpenHome simulator mode, could be first instantiation of camera)
            if (camera == null) {
                camera = new CameraSimulator(serialNo, log);
                cameraMap.put(serialNo, camera);
            }
        }
        camera.setSharedSecret(secret);
    }

    public String getPresetSerialNo() {
        String sn = OpenHomeProperties.getProperty("cameraSimulator.serialNo");
        if (sn != null && sn.length() > 1)
            return sn;
        else
            return "112233445566";
    }

    public String getSessionGw(String serialNo) {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null)
            return "";
        return camera.getSessionGw();
    }

    public String getXmppGw(String serialNo) {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null)
            return "";
        return camera.getXmppGw();
    }

    public String getStatus(String serialNo) {
        if (serialNo==null)
            return "Not Started";
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null)
            return "Not Started";
        return camera.getStatusStr();
    }

    public boolean finishedBootStrap(String serialNo) {
        if (serialNo==null)
            return false;
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null)
            return false;
        return camera.finishedBootStrap();
    }

    public CameraSimulator.XMPP_STATE getXMPPstate(String serialNo) {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null)
            return CameraSimulator.XMPP_STATE.IDLE;
        return camera.getXMPPstate();
    }

    public RtspURL getRtspSourceURL(String size) {
        RtspURL url = rtspSourceUrlMap.get(size);
        if (url==null)
            url = rtspSourceUrlMap.get("640x480");
        return url;
    }

    public void setRtspSourceURL(String size, String urlstr) throws IOException {
        if (urlstr==null || urlstr.length()==0)
            return;
        if (size==null)
            throw new IOException("Invalid size");
        RtspURL rtspUrl = new RtspURL(urlstr);
        rtspSourceUrlMap.put(size, rtspUrl);
        log.debug("setRtspSourceURL size="+size+" url="+urlstr);
    }

    public URL getMjpegSourceURL(String size) {
        URL url = mjpegSourceUrlMap.get(size);
        if (url==null)
            url = mjpegSourceUrlMap.get("640x480");
        return url;
    }

    public void setMjpegSourceURL(String size, String urlstr) throws IOException {
        if (urlstr==null || urlstr.length()==0)
            return;
        if (size==null)
            throw new IOException("Invalid size");
        URL url = new URL(urlstr);
        mjpegSourceUrlMap.put(size, url);
        log.debug("setMjpegSourceURL size="+size+" url="+urlstr);
    }

    /*
        URL for requesting live vide from camera, not rtsp/mjpeg source of the camera
     */
    public String getCameraVideoStreamingURL(String id) {
        /*
        String id = sizeToChannelIdMap.get(size);
        if (id==null)
            id = sizeToChannelIdMap.get("640x480");
        */

//        return "rtsp://" + "openhome/" + StreamingChannelsResource.GetStreamingChannelsRTSP.getStreamingChannelsRtspURI(id);
        return "rtsp://" + "relaysirius3.icontrol.com/openhome/" + "openhome/streaming/channels/"+id+"/rtsp";
    }

    /**
    public boolean isXMPPconnected(String serialNo) {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null)
            return false;
        return camera.isXMPPconnected();
    }

    public void xmppConnect(String serialNo, String hostNamePort) throws Exception {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null)
            throw new IOException("Unknown serialNo:"+serialNo);

        // find port if exists
        String host = null;
        int port = -1;
        int pos = hostNamePort.indexOf(":");
        if (pos == -1)
           throw new IOException("Invalid XMPP host:port");
        host = hostNamePort.substring(0, pos);
        port = Integer.parseInt(hostNamePort.substring(pos+1));

        camera.setXmppGw(host, port);
        camera.xmppConnect();
    }

    public void xmppDisconnect(String serialNo) throws Exception {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null)
            throw new IOException("Unknown serialNo:"+serialNo);
        camera.xmppDisconnect();
    }

    public void triggerMotion(String serialNo, String durationStr) throws Exception {
        CameraSimulator camera = cameraMap.get(serialNo);
        if (camera == null) {
            camera = new CameraSimulator(serialNo, log);
            cameraMap.put(serialNo, camera);
        }
        camera.triggerMotion(Integer.parseInt(durationStr));
    }

    // use by MediaUpLoader to send XMPP media upload notificaiton failture
    public void sendXmppNotification(String method, String action, String body) throws IOException {
        CameraSimulator camera = null;
        // grab first camera in the list, else throw exception
        Iterator<CameraSimulator> iter = cameraMap.values().iterator();
        if (iter.hasNext())
            camera = iter.next();

        camera.sendXmppNotification(method, action, body);
    }

    public String getLastSerialNo() {
        return lastSerialNo;
    }

    public String getLastRegistryURL() {
        return lastRegistryGwURL;
    }  */

}
