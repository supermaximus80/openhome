package com.icontrol.openhomesimulator.gateway.xmppserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import java.io.IOException;
import java.util.Iterator;



public class ServerIQResponseParser {
    private static final Log log = LogFactory.getLog(ServerIQResponseParser.class);

    private IQ iq;
    private int code;
    private String contentType;
    private int contentLen;
    private String bodyText;

    /*
        Decode Response IQ constructor
     */
    public ServerIQResponseParser(IQ iq) throws IOException {
        this.iq = iq;

        code = 500;
        contentType = null;
        contentLen = 0;
        bodyText = null;

        parse();
    }

    public IQ getIQ() {
        return iq;
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

    public String getPacketID() {
        return iq.getID();
    }

    private void parse() throws IOException {

        Element httpTunnel = iq.getChildElement();
        if (httpTunnel == null)
            throw new IOException("Unable to find http-tunnel element");
        String httpTunnelNameSpace = httpTunnel.getQName().getNamespace().getURI();
        //System.out.println("element name="+httpTunnel.getQName().getName());
        Element response = httpTunnel.element("response");
        if (response == null)
            throw new IOException("Unable to find response element");
        code = Integer.parseInt(response.attributeValue("code"));
        //System.out.println("    element name="+response.getQName().getName()+" code="+response.attributeValue("code"));

        Iterator<Element> iter = response.elementIterator();
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
                        bodyText = stripRepetitiveNamespace(bodyText, httpTunnelNameSpace);
                }
                // check for non-element text
                if (bodyText==null) {
                    bodyText = e.getText();
                }
            } else if ("Content-Type".equalsIgnoreCase(elementName)) {
                contentType = e.getText();
            } else if ("Content-Length".equalsIgnoreCase(elementName)) {
                contentLen = Integer.parseInt(e.getText());
            }
        }

        log.debug("ServerIQResponse received code="+code+" body="+bodyText);
    }

    static public String stripRepetitiveNamespace(String text, String ns) {
        String search = "xmlns="+"\""+ns+"\"";
        if (text.contains(search)) {
            text = text.replace(search,"") ;
        }
        return text;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("\nServerIQResponse: ");
        buffer.append("\nIQ: " + iq);
        buffer.append("\nContent Length: " + contentLen);
        buffer.append("\nContent type: " + contentType);
        buffer.append("\nBody text: " + bodyText);
        buffer.append("\n");
        return buffer.toString();
    }

}
