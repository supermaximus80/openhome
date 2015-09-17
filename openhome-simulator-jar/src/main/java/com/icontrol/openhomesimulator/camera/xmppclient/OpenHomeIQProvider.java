package com.icontrol.openhomesimulator.camera.xmppclient;


import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class OpenHomeIQProvider implements IQProvider {
    private String xmlBody = "";
    private boolean done;


    @Override
    public IQ parseIQ(XmlPullParser xmlPullParser) throws Exception {

        done = false;
        IQ iq = parsePacket(xmlPullParser) ;

        return iq;
    }

    private IQ parsePacket(XmlPullParser parser) throws Exception {
        IQ iqPacket = null;

        while (!done) {
            int eventType = parser.next();

            if (eventType == XmlPullParser.START_TAG) {
                String elementName = parser.getName();
                String namespace = parser.getNamespace();
                if (elementName.equals("request") ) {
                    iqPacket = parseRequest(parser);
                } else if (elementName.equals("response")) {
                    iqPacket = parseResponse(parser);
                }
            }
            else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("request") || parser.getName().equals("response")) {
                    done = true;
                }
            } else if (eventType == XmlPullParser.END_DOCUMENT) {
                done = true;
            }
        }


        return iqPacket;
    }

    final static String BODYHEADER = "_BODYHEADER";

    private IQ parseRequest(XmlPullParser parser) throws IOException, XmlPullParserException {
        String curHeaderName = null;

        OpenHomeRequestIQ iq = new OpenHomeRequestIQ() ;
        String method = parser.getAttributeValue("", "method");
        String action = parser.getAttributeValue("", "action");
        //System.out.println("found <request method="+method+" action="+action);
        iq.setMethod(method);
        iq.setAction(action);

        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("header")) {
                    String name = parser.getAttributeValue("", "name");
                    curHeaderName = name;
                }
                else if (parser.getName().equals("body")) {
                    curHeaderName = BODYHEADER;
                }
            } else if(eventType == XmlPullParser.TEXT) {
                String text = parser.getText();
                //System.out.println("found text header="+curHeaderName+" text="+text);
                if (BODYHEADER.equals(curHeaderName))
                    iq.setBodyText(text);
                else
                    iq.AddHeader(curHeaderName, text);
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("request")) {
                    done = true;
                } else if (parser.getName().equals("body")) {
                    curHeaderName = null;
                } else if (parser.getName().equals("header")) {
                    curHeaderName = null;
                }
            }
        }
        return iq;
    }

    private IQ parseResponse(XmlPullParser parser) throws IOException, XmlPullParserException {
        String curHeaderName = null;

        OpenHomeResponseIQ eiq = new OpenHomeResponseIQ() ;
        String codeStr = parser.getAttributeValue("", "code");
        //System.out.println("found <request method="+method+" action="+action);
        eiq.setCode(Integer.parseInt(codeStr));

        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("header")) {
                    String name = parser.getAttributeValue("", "name");
                    curHeaderName = name;
                }
                else if (parser.getName().equals("body")) {
                    curHeaderName = BODYHEADER;
                }
            } else if(eventType == XmlPullParser.TEXT) {
                String text = parser.getText();
                //System.out.println("found text header="+curHeaderName+" text="+text);
                if (BODYHEADER.equals(curHeaderName))
                    eiq.setBodyText(text);
                else
                    eiq.AddHeader(curHeaderName, text);
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("response")) {
                    done = true;
                } else if (parser.getName().equals("body")) {
                    curHeaderName = null;
                } else if (parser.getName().equals("header")) {
                    curHeaderName = null;
                }
            }
        }
        return eiq;
    }
}
