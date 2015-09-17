package com.icontrol.openhomesimulator.gateway.simplerelay;

import com.icontrol.openhomesimulator.camera.CameraSimulatorFactory;
import com.icontrol.openhomesimulator.camera.RtspURL;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class SimpleRelay implements RelayThreadStatusCallBack {

    private static final Logger log = LoggerFactory.getLogger(SimpleRelay.class);

    private String sid;
    private SimplePole cameraPole;
    private SimplePole userPole;
    private boolean isLive;
    private boolean isSimulatedCamera;
    private SimpleRelayThread userToCamera, cameraToUser;
    private long lastRead;
    private long totalReadBytes;
    private String channelID;

    public SimpleRelay(String sid) {
        this.sid = sid;
        this.cameraPole = null;
        this.userPole = null;
        isLive = true;
        isSimulatedCamera = false;
        userToCamera = null;
        cameraToUser = null;
        lastRead = 0;
        totalReadBytes = 0;
        this.channelID = "1";
    }

    public boolean isLive() {
        return isLive;
    }

    public void setIsLive(boolean isLive) {
        this.isLive = isLive;
    }

    public void AddCameraPole(SimplePole pole)  {
        if (cameraPole != null) {
            log.debug("New Camera pole arrived. Closing exisitng/old camera pole.");
            cameraPole.close();
            if (userPole != null) {
                userPole.close();
                userPole = null;
            }
        }
        cameraPole = pole;
        isSimulatedCamera = false;
        checkStartStreaming();
    }

    public void AddUserPole(SimplePole pole, String channelID) {
        if (userPole != null) {
            userPole.close();
            cameraPole.close();
            cameraPole = null;
        }
        userPole = pole;
        this.channelID = channelID;
        checkStartStreaming();
    }

    public void addSimulatedCameraPole(String rtspHost, int port)  {
        if (cameraPole != null)
            cameraPole.close();

        // if rtsp host not specified, use camera simulator rtsp source
        if (rtspHost == null)  {
            try {
                RtspURL rtspURL =  new RtspURL(CameraSimulatorFactory.getInstance().getCameraVideoStreamingURL(null));
                rtspHost = rtspURL.getHost();
                port = rtspURL.getPort();
            } catch (Exception ex) {}
        }

        // connect to rtsp source
        Socket socket = null;
        try {
            socket = new Socket(rtspHost, port);

            if (socket != null) {
                cameraPole = new SimplePole(sid, socket);
                isSimulatedCamera = true;

                checkStartStreaming();
            } else {
                log.error("addSimulatedCameraPole failed socket or is/os == null");
            }
        } catch(UnknownHostException unknownHost) {
            log.error("SimpleRelay unknownHost error "+unknownHost, unknownHost);
        } catch(IOException ex){
            log.error("SimpleRelay caught "+ex, ex);
        }
    }

    private void checkStartStreaming() {
        if (userPole == null) {
            log.debug("Relay: Camera pole connected, waiting for user pole");
            return;
        } else if (cameraPole == null) {
            log.debug("Relay: User pole connected, waiting for camera pole");
            return;
        }

        log.debug("Relay: Both Camera and User poles connected, start streaming. channelID="+channelID);
        try {
            // translate request line into correct rtsp request
            String rtspURL = CameraSimulatorFactory.getInstance().getCameraVideoStreamingURL(channelID);
            ByteBuffer buffer = translateUserBuffer(userPole.getByteBuffer(), userPole.getFirstRequestLine(), rtspURL);   // TODO, use correct rtsp url

            // write content of bytebuffer, if any, to outputstream
            writeByteBufferToOutputStream(buffer, cameraPole.getOutputStream());

            // both poles are connected, start streaming
            // user --> camera
            userToCamera = new SimpleRelayThread("user->camera", this, userPole.getInputStream(), cameraPole.getOutputStream(), false, log);
            // camera --> user
            cameraToUser = new SimpleRelayThread("camera->user", this, cameraPole.getInputStream(), userPole.getOutputStream(), true, log);

            userToCamera.getTaskThread().start();
            cameraToUser.getTaskThread().run();
            log.debug("Relay: Finished streaming relay sid="+sid);
        } catch (Exception ex) {
            log.error("SimpleRelay checkStartStreaming caught "+ex);
        }
    }

    @Override
    public void setLastRead(long timems) {
        lastRead = timems;
    }

    @Override
    public void addBytesRead(int len) {
        totalReadBytes += len;
    }

    @Override
    public void close() {
        log.debug("relay sid="+sid+" close().");
        isLive = false;
        if (userPole != null) {
            userPole.close();
        }
        if (cameraPole != null) {
            cameraPole.close();
        }
    }

    private void writeByteBufferToOutputStream(ByteBuffer buffer, OutputStream os) throws IOException {
        //log.debug("writeByteBufferToOutputStream hasRemaining:"+buffer.hasRemaining()+" limit="+buffer.limit()+" position="+buffer.position());
        if (buffer.hasRemaining() && os != null) {
            os.write(buffer.array(), 0, buffer.limit());
            os.flush();
        }
    }


    /*
     * this method performs any necessary translation on the data contained in
     * the user buffer - eg. the data in the user request - before passing it on
     * to the device on the other side of the relay - eg. the camera.
     *
        input:  DESCRIBE rtsp://localhost:10090/relay/user/<sid> RTSP/1.0
        output: rtsp://<cameraUri> RTSP/1.0
     */
    ByteBuffer translateUserBuffer(ByteBuffer ubuff, String reqFirstLine, String replaceURI) {
        try {
            if (reqFirstLine==null)
                return ubuff;

            int pos = reqFirstLine.indexOf("rtsp://") ;
            if (pos == -1)
                return ubuff;
            int pos2 = reqFirstLine.indexOf(" RTSP/1", pos) ;
            if (pos2 == -1)
                return ubuff;

            String find = reqFirstLine.substring(pos, pos2);
            String line = new String(ubuff.array(), 0, ubuff.limit());
            log.debug("Relay.translateUserBuffer translate from:\n" + line + "to:\n"+replaceURI);

            if (line.indexOf(find) != -1) {
                line = line.replace(find, replaceURI);
            }
            System.arraycopy(line.getBytes(), 0, ubuff.array(), 0, line.length());
            ubuff.limit(line.length());
            ubuff.position(0);
            line = new String(ubuff.array(), 0, ubuff.limit());
            log.debug("Relay.translateUserBuffer resulting buffer:\n"+line);
        } catch (Exception e) {
                log.warn("Relay.translateUserBuffer exception e:" + e);
        }
        return ubuff;
    }

}
