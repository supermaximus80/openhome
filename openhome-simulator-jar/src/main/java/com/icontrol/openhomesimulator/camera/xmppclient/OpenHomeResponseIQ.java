package com.icontrol.openhomesimulator.camera.xmppclient;

import org.jivesoftware.smack.packet.IQ;

import java.util.HashMap;
import java.util.Map;

public class OpenHomeResponseIQ extends IQ {
    private Map<String, String> headerMap;
    private int code;
    private String contentType;
    private String bodyText;

    public OpenHomeResponseIQ() {
        super();
        headerMap = new HashMap<String, String>() ;
        code=-1;
        contentType = "application/xml; charset=\""+"UTF-8\"";
        bodyText = null;
        // set default IQ type
        super.setType(Type.RESULT);
    }

    public String getChildElementXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<http-tunnel xmlns=\"http://icontrol.com/http-tunnel/v1\">"+"\n") ;
        sb.append("<response code=\""+code+"\">"+"\n");
        //headers
        for (String key : headerMap.keySet()) {
            sb.append("<header name=\""+key+"\">"+"\n") ;
            sb.append(headerMap.get(key));
            sb.append("</header>"+"\n") ;
        }
        // body
        sb.append("<body name=\"body\">"+"\n");
        if (bodyText != null)
            sb.append(bodyText+"\n");
        sb.append("</body>"+"\n");

        //
        sb.append("</response>"+"\n");
        sb.append("</http-tunnel>"+"\n");

        return sb.toString();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String type) {
        contentType = type;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public void AddHeader(String name, String text) {
        headerMap.put(name, text);
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String text) {
        // strip out   <?xml version="1.0" encoding="UTF-8"?>
        int pos = text.indexOf("<?xml") ;
        if (pos != -1) {
            pos = text.indexOf("?>", pos);
            if (pos != -1)
                text = text.substring(pos+"?>".length());
        }
        bodyText = text;
    }


}
