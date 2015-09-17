package com.icontrol.android.openhomesimulator.simplerelay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.TimerTask;


public class SimplePole extends TimerTask {

    private static final Logger log = LoggerFactory.getLogger(SimplePole.class);

    private String sid;

    private InputStream is;
    private OutputStream os;
    private Socket socket;
    private ByteBuffer buffer;
    private SimpleRelayManager relayManager;
    private String firstLine;

    public SimplePole(String sid, Socket socket) throws IOException {
        this.sid = sid;
        this.socket = socket;
        is = socket.getInputStream();
        os = socket.getOutputStream();
        buffer =  ByteBuffer.allocate(0);
        relayManager = null;
        firstLine = null;
    }

    public SimplePole(SimpleRelayManager relayManager, Socket socket) throws IOException {
        this.sid = null;
        this.socket = socket;
        is = socket.getInputStream();
        os = socket.getOutputStream();
        buffer =  ByteBuffer.allocate(0);
        this.relayManager = relayManager;
        firstLine = null;
    }

    public SimplePole(String sid, InputStream is, OutputStream os) throws IOException {
        this.sid = sid;
        this.socket = null;
        is = socket.getInputStream();
        os = socket.getOutputStream();
        buffer =  ByteBuffer.allocate(0);
        relayManager = null;
        firstLine = null;
    }

    public InputStream getInputStream() {
        return is;
    }

    public OutputStream getOutputStream() {
        return os;
    }

    public String getSid() {
        return sid;
    }

    public ByteBuffer getByteBuffer() {
        return buffer;
    }

    public String getFirstRequestLine() {
        return firstLine;
    }

    public void close() {
        log.debug("SimplePole close");
        try {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
            if (socket != null)
                socket.close();
        } catch(IOException ex){
                log.error("SimplePole close caught "+ex);
        }

    }

    /*
        run
     */
    @Override
    public void run() {
        try {
            buffer =  ByteBuffer.allocate(8192);
            int len = -1;
            while ( -1 != (len = is.read(buffer.array(), buffer.position(), buffer.limit()-buffer.position()))) {
                buffer.position( buffer.position() + len );
                firstLine = getFirstLine(buffer);
                if (firstLine != null)  {
                    String sid = getSessionID(firstLine);
                    log.debug("Relay: Incomng RelayPole Request="+firstLine);
                    if (relayManager != null) {
                        SimpleRelay relay = relayManager.getRelay(sid) ;
                        if (relay != null) {
                            if (relayManager.isUserPole(firstLine)) {
                                relay.AddUserPole(this, getChannelID(firstLine));
                                // TEST ONLY, add simul camera
                                // TEST ONLY relay.addSimulatedCameraPole(null, 0);
                                break;
                            } else if (relayManager.isCameraPole(firstLine)) {
                                relay.AddCameraPole(this);
                                break;
                            } else {
                                log.error("Reject toUser connection. Invalid firstLine "+firstLine);
                                close();
                            }
                        } else {
                            log.error("Reject toUser connection. Unable to find sid="+sid);
                            close();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("SimplePole.run caught "+ex, ex);
        }

    }

    /*
        read first line and extract sid
     */
    private String processIncoming() throws IOException {
        if (is==null)
            return null;

        ByteBuffer buffer =  ByteBuffer.allocate(8192);
        int len = -1;
        while ( -1 != (len = is.read(buffer.array(), buffer.position(), buffer.limit()))) {
            buffer.position( buffer.position() + len );
            String firstLine = getFirstLine(buffer);
            if (firstLine != null)  {
                String sid = getSessionID(firstLine);
                log.debug("FirstLine="+firstLine+" sid="+sid);
                return sid;
            }
        }
        return null;
    }

    /*
        extract sid from request line
     */
    private String getSessionID(String str) {
        if (str==null)
            return null;
        str = str.toLowerCase();

        String basepath = relayManager.isUserPole(firstLine) ? SimpleRelayManager.relayUserPath :  SimpleRelayManager.relayCameraPath;
        int pos = str.indexOf(basepath);
        if (pos == -1)
            return null;
        int pos2 = str.indexOf("?", pos);
        if (pos2==-1)
            pos2 = str.indexOf(" ", pos);
        if (pos2==-1)
            pos2 = str.indexOf("\r\n", pos);
        if (pos2==-1)
            pos2 = str.length();
        return str.substring(pos+basepath.length(), pos2)  ;
    }

    /*
        extract channel ID from request line
     */
    private String getChannelID(String str) {
        if (str==null)
            return "1";
        str = str.toLowerCase();

        try {
            String basepath = relayManager.isUserPole(firstLine) ? SimpleRelayManager.relayUserPath :  SimpleRelayManager.relayCameraPath;
            int pos = str.indexOf(basepath);
            if (pos == -1)
                return "1";
            pos = str.indexOf("?channel=");
            if (pos==-1)
                return "1";
            int pos2 = str.indexOf("&", pos);
            if (pos2 == -1)
                pos2 = str.indexOf(" ", pos);
            if (pos2 == -1)
                pos2 = str.length();
            String sChanId = str.substring(pos+"?channel=".length(), pos2)  ;
            Integer.parseInt(sChanId) ;    // make sure it is a number
            return sChanId;
        } catch (Exception ex) {
            return "1";
        }
    }

    /*
     * Leave buffer as is if we don't have a full line
     * If we do, then leave buffer pointing to end of first line
     */
    private String getFirstLine(ByteBuffer buffer) {
        int position = buffer.position();
        if (position < 5) {
            return null;
        }
        int limit = buffer.limit();

        buffer.flip();
        try {
            String req = new String(buffer.array(), buffer.position(), buffer.limit());
            int index = req.indexOf("\r\n\r\n");
            if (index == -1) {
                log.debug("getFirstLine early return, end of request not found yet. req:" + req);
                return null;       // not found, return
            }

            int endfirstline = req.indexOf("\r\n");
            int advancelen = (endfirstline == index ? index + "\r\n\r\n".length() : endfirstline + "\r\n".length());

            // found end of line
            position = buffer.position() + advancelen;
            limit = buffer.limit();
            return req.substring(0, position);
        } finally {
            // not enough data: revert
            buffer.position(position);
            buffer.limit(limit);
        }
    }
}
