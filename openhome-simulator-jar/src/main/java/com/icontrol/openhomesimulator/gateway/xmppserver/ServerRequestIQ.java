package com.icontrol.openhomesimulator.gateway.xmppserver;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import java.util.Iterator;
import java.util.Map;


public class ServerRequestIQ extends IQ {

    protected static DocumentFactory docFactory = DocumentFactory.getInstance();

    public ServerRequestIQ(JID fromAddress, JID toAddress, String method, String action, Map<String, String> headerMap, String bodyText) {
        super(IQ.Type.set);

        if (fromAddress != null)
            super.setFrom(fromAddress);
        if (toAddress != null)
            super.setTo(toAddress);

        addOpenHomeElement(method, action, headerMap, bodyText);
    }

    private void addOpenHomeElement(String method, String action, Map<String, String> headerMap, String bodyText) {
        Element ht = super.setChildElement("http-tunnel", "http://icontrol.com/http-tunnel/v1") ;
        Element reqE = ht.addElement("request");
        reqE.addAttribute("method", method.toUpperCase()) ;
        reqE.addAttribute("action", action) ;
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
