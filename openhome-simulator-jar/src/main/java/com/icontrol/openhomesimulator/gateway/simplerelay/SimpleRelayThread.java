package com.icontrol.openhomesimulator.gateway.simplerelay;

import org.slf4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;

public class SimpleRelayThread implements Runnable {

    Logger log;
    String id;
    RelayThreadStatusCallBack relay;
    InputStream is;
    OutputStream os;
    boolean toUser;
    Thread taskThread;
    int curInterleavedChannel;
    long totalBytesXferred;

    public SimpleRelayThread(String id, RelayThreadStatusCallBack relay, InputStream is, OutputStream os, boolean toUser, Logger log) {
        this.log = log;
        this.id = id;
        this.relay = relay;
        this.is = is;
        this.os = os;
        this.toUser = toUser;
        this.taskThread = new Thread(this);
        this.curInterleavedChannel = 0;
        this.totalBytesXferred = 0;
    }

    public Thread getTaskThread() {
        return taskThread;
    }

    @Override
    public void run() {
        try {
            log.debug("Relay: ["+id+"] Thread run");
            byte[] bytes = new byte[16*1024];
            int len = -1;
            while (relay.isLive() && -1 != (len = is.read(bytes, 0, bytes.length))) {
                relay.setLastRead(System.currentTimeMillis());
                if (len != 0) {
                    if (toUser)
                        relay.addBytesRead(len);
                    totalBytesXferred += len;

                    os.write(bytes, 0, len);
                    //log.debug("SimpleRelayThread wrote "+read+" toUser="+toUser+ " text="+(new String(bytes,0, read)));
                }
            }
        } catch (Exception ex) {
            log.error("Relay: ["+id+"] SimpleRelayThread caught "+ex);
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

        log.debug("Relay: ["+id+"] SimpleRelayThread finished run. totalBytesXferred="+totalBytesXferred);

        /* todo
        if (toUser) {
            if (System.currentTimeMillis() - relay.getLastRead() > relay.getServerTimeout()) {
                relay.close();
            }
        }
        */
    }

}
