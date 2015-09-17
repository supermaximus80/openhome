package com.icontrol.openhomesimulator.camera.mediatunnel;

import org.slf4j.Logger;

public class TranslateRTSP {

    private String rtspSourceStr;
    private Logger log;
    private int curInterleavedChannel;

    public TranslateRTSP(String rtspSourceStr, Logger log) {
        this.rtspSourceStr = rtspSourceStr;
        this.log = log;

        curInterleavedChannel = 0;
    }

    public int translate(byte[] bytes, int len) {
        String str = new String(bytes, 0, len);
        if (str.startsWith("DESCRIBE"))
            return translateRequestLine(str, bytes, len);
        else if (str.startsWith("PLAY"))
            return translateRequestLine(str, bytes, len);
        //else if (str.startsWith("SETUP"))
        //    return translateSETUP(str, bytes, len);
        else
            return len;
    }

    private int translateSETUP(String str, byte[] bytes, int len) {
        // modify transport line
        int pos = str.indexOf("Transport: ");
        if (pos == -1) {
            log.error("Error! SETUP does not contain Transport line");
            return len;
        }
        int pos2 = str.indexOf("\r\n", pos);
        if (pos2 == -1) {
            log.error("Error! SETUP does not contain end of line for Transport");
            return len;
        }
        String find = str.substring(pos, pos2);
        String replacement = "Transport: RTP/AVP/TCP;unicast;interleaved="+ String.format("%d-%d", curInterleavedChannel, curInterleavedChannel+1) ;
        curInterleavedChannel += 2;
        str = str.replace(find, replacement) ;
        System.arraycopy(str.getBytes(), 0, bytes, 0, str.length());

        log.debug("translateSETUP Replaced "+find+" with "+replacement);
        return str.length();
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

        log.debug("translateRequestLine Replaced "+find+" with "+replacement);
        return str.length();
    }
}
