package com.icontrol.android.openhomesimulator.simplerelay;

import com.icontrol.android.openhomesimulator.util.OpenHomeProperties;
import com.icontrol.android.openhomesimulator.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLServerSocket;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleRelayManager {

    /*
        static variables and methods
     */
    static Object monitor = new Object();

    static private SimpleRelayManager manager = null;

    static private int CAMERA_RELAY_INCOMING_PORT = 10090;
    static private int USER_RELAY_INCOMING_PORT = 10092;

    static public SimpleRelayManager getInstance() {
        synchronized(monitor) {
            if (manager == null) {
                try {
                    manager = new SimpleRelayManager(CAMERA_RELAY_INCOMING_PORT, USER_RELAY_INCOMING_PORT);
                    manager.start();
                } catch (Exception e) {
                    log.error("SimpleRelayManager caught "+e);
                    return null;
                }
            }
        }
        return manager;
    }

    static public SimpleRelayManager createInstance() throws Exception {
        synchronized(monitor) {
            if (manager == null) {
                manager = new SimpleRelayManager(CAMERA_RELAY_INCOMING_PORT, USER_RELAY_INCOMING_PORT);
                manager.start();
            }
        }
        return manager;
    }

    static public void end() {
        synchronized(monitor) {
            if (manager != null) {
                manager.stopRelayManager();
                manager = null;
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(SimpleRelayManager.class);

    public static final String relayUserPath = "relay/user/" ;  // full uri is relay/user/[sid]
    public static final String relayCameraPath = "relay/camera/" ;  // full uri is relay/camera/[sid]
    // TODO add timer to check relay expiration

    /*
        class variables and methods
     */
    int cameraPort;
    int userPort;
    RelayPoleListener cameraTunnelListener;
    RelayPoleListener userPoleListener;
    Map<String, SimpleRelay> relayMap ;
    int nextSessionID ;
    boolean isLive;
    String userPrefix;
    String cameraPolePrefix;

    static boolean useSSLForCameraPole;
    static boolean useSSLForUserPole;

    static {
        useSSLForCameraPole= OpenHomeProperties.getProperty("tls.relay.cameraPole", true);
        useSSLForUserPole= OpenHomeProperties.getProperty("tls.relay.userPole", false);
        log.debug("SimpleRelayManager - useSSLForCameraPole = " + useSSLForCameraPole);
        log.debug("SimpleRelayManager - useSSLForUserPole = " + useSSLForUserPole);
    }

    private SimpleRelayManager(int cameraPort, int userPort) throws Exception {
        this.cameraPort = cameraPort;
        this.userPort = userPort;
        relayMap = new ConcurrentHashMap<String, SimpleRelay>();
        nextSessionID = 1;
        isLive = true;

        // create socket listen objects, one for SSL tunnel for uesr (Quicktime player)
        cameraTunnelListener = new RelayPoleListener(this, useSSLForCameraPole, cameraPort, "cameraPole");
        userPoleListener =  new RelayPoleListener(this, useSSLForUserPole, userPort, "userPole");
    }

    public void start() {
        cameraTunnelListener.start();
        userPoleListener.start();
    }

    public int getCameraListenPort() {
        return cameraPort;
    }

    public int getUserListenPort() {
        return userPort;
    }

    public String getRelayUserPath() {
        return relayUserPath;
    }

    public String getRelayCameraPath() {
        return relayCameraPath;
    }

    public void setContextPath(HttpServletRequest request) {
        userPrefix = "rtsp://" + request.getServerName() + ":" + getUserListenPort() +"/"+ getRelayUserPath();

        String protocol = "http";
        if (useSSLForCameraPole) {
            protocol = "https";
        }
        cameraPolePrefix = protocol + "://" + request.getServerName() + ":" + getCameraListenPort() +"/"+ getRelayCameraPath();
    }

    public void setIsLive(boolean val) {
        isLive = val;
    }

    public boolean getIsLive() {
        return isLive;
    }

    public void stopRelayManager() {
        try {
            isLive = false;
            cameraTunnelListener.stopListening();
            userPoleListener.stopListening();
        } catch (Exception e) {
            log.error("stopRelayManager caught "+e);
        }
    }

    public SimpleRelay getRelay(String sid) {
        SimpleRelay relay = relayMap.get(sid);
        if (relay == null) {
            log.error("getRelay unable to find sid="+sid);
        }
        return relay;
    }

    public void removeRelay(String sid) {
        relayMap.remove(sid) ;
    }

    /*
        returns user relay URL
     */
    public String createRelayAndGetUserURL(StringBuilder responseSB) {
        String sid = Long.toString(nextSessionID++);
        SimpleRelay relay = new SimpleRelay(sid) ;
        relayMap.put(sid, relay);

        return getUserRelayURL(sid);
    }

    public String createRelay() {
        String sid = Long.toString(nextSessionID++);
        SimpleRelay relay = new SimpleRelay(sid) ;
        relayMap.put(sid, relay);

        return sid;
    }

    public String getUserRelayURL(String sid) {
        return userPrefix + sid;
    }

    public String getCameraRelayURL(String sid) {
        return cameraPolePrefix + sid;
    }

    public String getCurSID() {
        return Long.toString(nextSessionID);
    }

    public boolean isUserPole(String reqLine) {
        if (reqLine.contains(relayUserPath))
            return true;
        else
            return false;
    }

    public boolean isCameraPole(String reqLine) {
        if (reqLine.contains(relayCameraPath))
            return true;
        else
            return false;
    }

    public class RelayPoleListener extends Thread {

        SimpleRelayManager manager;
        boolean useSSL;
        int listenPort;
        String name;
        ServerSocket providerSocket;

        RelayPoleListener(SimpleRelayManager manager, boolean useSSL, int listenPort, String name) throws Exception {
            this.manager = manager;
            this.useSSL = useSSL;
            this.listenPort = listenPort;
            this.name = name;

            //create a server socket
            providerSocket = createBindServerSocket(useSSL, listenPort);
        }

        void stopListening() {
            try {
                 providerSocket.close();
                 providerSocket = null;
             } catch (IOException e) {
                 log.error("stopListening caught "+e);
             }
        }

        private ServerSocket createBindServerSocket(boolean useSSL, int port) throws Exception  {
            ServerSocket serverSocket;
            if (useSSL) {
                SSLServerSocket sslSocket = Utilities.getSSLServerSocket();
                printServerSocketInfo( sslSocket) ;
                serverSocket = sslSocket;
            } else {
                serverSocket = new ServerSocket();
            }

            // bind to listenPort
            serverSocket.setReuseAddress(true);
            InetSocketAddress listen = new InetSocketAddress(port);
            serverSocket.bind(listen, 4);

            return serverSocket;
        }

        @Override
        public void run() {
            //log.debug("SimpleRelayManager Starting...");
            Socket connection = null;
            try{
                if (providerSocket == null) {
                    log.error("SimpleRelayManager Null providerSocket error. Trying to create new socket");
                    providerSocket = createBindServerSocket(useSSL, listenPort);
                }

                while (isLive) {
                    //Wait for connection
                    connection = providerSocket.accept();

                    // create a pole and add to relay manager
                    if (connection != null) {
                        log.debug("Relay: Incoming relay connection to "+name+" at port "+listenPort+" useSSL="+useSSL);
                        Timer timer = new Timer() ;
                        timer.schedule(new SimplePole(manager, connection), 10);
                    }
                }
            } catch(Exception ex) {
                log.error("SimpleRelayManager caught "+ex, ex);
            } finally {
                try{
                    if (providerSocket != null)
                        providerSocket.close();
                }
                catch(IOException ex){
                    log.error("SimpleRelayManagerInner caught "+ex, ex);
                }
            }
            log.debug("SimpleRelay run() finished");
        }

        private void printServerSocketInfo(SSLServerSocket s) {
            try {
              log.debug("Server socket class: "+s.getClass());
              log.debug("   Need client authentication = "
                 +s.getNeedClientAuth());
              log.debug("   Want client authentication = "
                 +s.getWantClientAuth());
              log.debug("   Use client mode = "
                 +s.getUseClientMode());
            } catch (Exception ex) {}
       }
    }
}
