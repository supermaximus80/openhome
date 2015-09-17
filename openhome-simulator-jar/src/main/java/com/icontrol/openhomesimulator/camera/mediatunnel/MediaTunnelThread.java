package com.icontrol.openhomesimulator.camera.mediatunnel;

import com.icontrol.openhomesimulator.gateway.simplerelay.RelayThreadStatusCallBack;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

public class MediaTunnelThread implements Runnable {

    Logger log;
    String id;
    RelayThreadStatusCallBack relay;
    InputStream is;
    OutputStream os;
    boolean toCamera;
    Thread taskThread;
    int curInterleavedChannel;
    TranslateRTSP translateRTSP;

    public MediaTunnelThread(String id, RelayThreadStatusCallBack relay, InputStream is, OutputStream os, boolean toCamera, TranslateRTSP translateRTSP, Logger log) {
        this.log = log;
        this.id = id;
        this.relay = relay;
        this.is = is;
        this.os = os;
        this.toCamera = toCamera;
        this.taskThread = new Thread(this);
        this.curInterleavedChannel = 0;
        this.translateRTSP = translateRTSP;
    }

    public Thread getTaskThread() {
        return taskThread;
    }

    @Override
    public void run() {
        //log.debug("["+id+"] Thread run");
        try {
            byte[] bytes = new byte[2048];
            int len = -1;
            try {
                while (relay.isLive() && -1 != (len = is.read(bytes, 0, bytes.length))) {
                    relay.setLastRead(System.currentTimeMillis());
                    if (len != 0) {
                        if (!toCamera) {
                            relay.addBytesRead(len);
                        }
                        // modify RTSP messages
                        if (toCamera && translateRTSP != null)
                            len = translateRTSP.translate(bytes, len);

                        os.write(bytes, 0, len);
                        //log.debug("SimpleRelayThread wrote "+read+" incoming="+incoming+ " text="+(new String(bytes,0, read)));
                    }
                }
            } catch (SocketTimeoutException ex) {}
        } catch (java.net.SocketException ex) {
            log.error("MediaTunnelThread caught "+ex);
        } catch (Exception ex) {
            log.error("["+id+"] MediaTunnelThread caught "+ex, ex);
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            }
            catch (Exception ex) {
                log.error("SimpleRelayThread finally caught "+ex);
            }
        }

        log.debug("["+id+"] MediaTunnelThread finished run");

        /* todo
        if (incoming) {
            if (System.currentTimeMillis() - relay.getLastRead() > relay.getServerTimeout()) {
                relay.close();
            }
        }
        */
    }

}
