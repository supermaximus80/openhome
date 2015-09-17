package com.icontrol.openhomesimulator.camera.xmppclient;

import java.io.IOException;

public interface XmppClientSend {

    public void sendXmppNotification(String method, String action, String body) throws IOException;

    public void sendXmppNotification(String method, String action, String body, String packetId) throws IOException;
}
