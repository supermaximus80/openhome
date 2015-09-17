package com.icontrol.openhomesimulator.gateway.xmppserver;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import java.util.Iterator;
import java.util.Map;


public class ServerIQResponse extends IQ {
    protected static DocumentFactory docFactory = DocumentFactory.getInstance();

    private int code;
    private String contentType;
    private int contentLen;
    private String bodyText;

    /*
        create Response IQ constructor
     */
    public ServerIQResponse(JID fromAddress, JID toAddress, int code, String bodyText, Map<String, String> headerMap) {
        super(Type.result);

        if (fromAddress != null)
            super.setFrom(fromAddress);
        if (toAddress != null)
            super.setTo(toAddress);

        this.code = code;
        this.contentType = "application/xml; charset=\"UTF-8\"";
        this.contentLen = bodyText == null ? 0 : bodyText.length();
        this.bodyText = bodyText;
        // create XML
        addOpenHomeElement(code, bodyText, headerMap);
    }

    public int getCode() {
        return code;
    }

    public String getContentType() {
        return contentType;
    }

    public int getContentLen() {
        return contentLen;
    }

    public String getBodyText() {
        return bodyText;
    }

    private void addOpenHomeElement(int code, String bodyText, Map<String, String> headerMap) {
        Element ht = super.setChildElement("http-tunnel", "http://icontrol.com/http-tunnel/v1") ;
        Element reqE = ht.addElement("response");
        reqE.addAttribute("code", Integer.toString(code)) ;
        // headers
        if (headerMap != null) {
            for ( Iterator<String> i = headerMap.keySet().iterator(); i.hasNext(); ) {
                String name = i.next();
                String text = headerMap.get(name);
                Element header = reqE.addElement("header") ;
                header.addAttribute("name",name);
                header.add(docFactory.createText(text));
            }
        }
        // body
        Element body = reqE.addElement("body") ;
        body.addAttribute("name","body");
        if (bodyText != null)
            body.add(docFactory.createText(bodyText));
    }

}
