package com.icontrol.openhomesimulator.gateway.simplerelay;

import org.slf4j.Logger;

public class SimpleRelayTranslateRTSP {

    private String rtspSourceStr;
    private Logger log;
    private int curInterleavedChannel;

    public SimpleRelayTranslateRTSP(String rtspSourceStr, Logger log) {
        this.rtspSourceStr = rtspSourceStr;
        this.log = log;

        curInterleavedChannel = 0;
    }

    public int translate(byte[] bytes, int len) {
        String str = new String(bytes, 0, len);
        if (str.startsWith("DESCRIBE"))
            return translateRequestLine(str, bytes, len);
        else
            return len;
    }

    private int translateRequestLine(String str, byte[] bytes, int len) {
        // modify transport line
        int pos = str.indexOf("rtsp://");
        if (pos == -1) {
            log.error("Error! request line does not contain rtsp://");
            return len;
        }
        int pos2 = str.indexOf(" RTSP/1", pos);
        if (pos2 == -1) {
            log.error("Error! request line does not contain  RTSP/1");
            return len;
        }
        String find = str.substring(pos, pos2);
        String replacement = rtspSourceStr ;
        str = str.replace(find, replacement) ;
        System.arraycopy(str.getBytes(), 0, bytes, 0, str.length());

        log.debug("SimpleRelayTranslateRTSP Replaced "+find+" with "+replacement);
        return str.length();
    }
}
