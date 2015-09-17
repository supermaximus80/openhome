package com.icontrol.openhomesimulator.camera.xmppclient;

import org.jivesoftware.smack.packet.IQ;

import java.util.HashMap;
import java.util.Map;

public class OpenHomeRequestIQ extends IQ {
    private Map<String, String> headerMap;
    private String method;
    private String action;
    private String bodyText;

    public OpenHomeRequestIQ() {
        super();
        headerMap = new HashMap<String, String>() ;
        method="";
        action="";
        bodyText = null;
        super.setType(Type.SET);
    }

    public String getChildElementXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<http-tunnel xmlns=\"http://icontrol.com/http-tunnel/v1\">") ;
        sb.append("<request method=\""+method+"\" action=\""+action+"\">");
        //headers
        for (String key : headerMap.keySet()) {
            sb.append("<header name=\""+key+"\">") ;
            sb.append(headerMap.get(key));
            sb.append("</header>") ;
        }
        // body
        sb.append("<body name=\"body\">");
        if (bodyText != null)
            sb.append(bodyText);
        sb.append("</body>");

        //
        sb.append("</request>");
        sb.append("</http-tunnel>");

        return sb.toString();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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
        /*
        int pos = text.indexOf("<?xml") ;
        if (pos != -1) {
            pos = text.indexOf("?>", pos);
            if (pos != -1)
                text = text.substring(pos+"?>".length());
        }*/
        bodyText = text;
    }
}
