package com.icontrol.openhomesimulator.gateway.xmppserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import java.io.IOException;
import java.util.Iterator;


public class ServerRequestIQParser {

    private static final Log log = LogFactory.getLog(ServerRequestIQParser.class);

    private IQ iq;
    private String method;
    private String action;
    private String contentType;
    private int contentLen;
    private String bodyText;

    public ServerRequestIQParser(IQ iq) throws IOException {
        this.iq = iq;
        this.method = null;
        this.action = null;
        this.contentLen = 0;
        this.contentType = null;
        this.bodyText = null;

        parse();
    }

    private void parse() throws IOException {

        Element httpTunnel = iq.getChildElement();
        if (httpTunnel == null)
            throw new IOException("Unable to find http-tunnel element");
        String httpTunnelNameSpace = httpTunnel.getQName().getNamespace().getURI();
        //System.out.println("element name="+httpTunnel.getQName().getName());
        Element request = httpTunnel.element("request");
        if (request == null)
            throw new IOException("Unable to find request element");
        this.method = request.attributeValue("method");
        this.action = request.attributeValue("action");

        Iterator<Element> iter = request.elementIterator();
        while (iter.hasNext()) {
            Element e = iter.next();
            String elementName = e.getQName().getName();

            if ("body".equalsIgnoreCase(elementName)) {
                for ( Iterator<Element> i = e.elementIterator(); i.hasNext(); ) {
                    Element inner = i.next();
                    // remove unnecessary namespace
                    //log.debug("name="+inner.getName()+" qualifiedName="+inner.getQualifiedName());
                    org.dom4j.QName qName = new org.dom4j.QName(inner.getName(), null, inner.getQualifiedName());
                    inner.setQName(qName);

                    // set bodyText
                    bodyText = inner.asXML();
                    if (bodyText != null)
                        bodyText = ServerIQResponseParser.stripRepetitiveNamespace(bodyText, httpTunnelNameSpace);
                }
                // check for non-element text
                if (bodyText==null) {
                    bodyText = e.getText();
                }
            } else if ("Content-Type".equalsIgnoreCase(elementName)) {
                this.contentType = e.getText();
            } else if ("Content-Length".equalsIgnoreCase(elementName)) {
                this.contentLen = Integer.parseInt(e.getText());
            }
        }

        log.debug("ServerRequestIQ received method="+method+" action="+action+" body="+bodyText);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("\nServerRequestIQ: ");
        buffer.append("\nIQ: " + iq);
        buffer.append("\nContent Length: " + contentLen);
        buffer.append("\nContent type: " + contentType);
        buffer.append("\nBody text: " + bodyText);
        buffer.append("\n");
        return buffer.toString();
    }

    public IQ getIq() {
        return iq;
    }

    public String getMethod() {
        return method;
    }

    public String getAction() {
        return action;
    }

    public String getBodyText() {
        return bodyText;
    }
}
