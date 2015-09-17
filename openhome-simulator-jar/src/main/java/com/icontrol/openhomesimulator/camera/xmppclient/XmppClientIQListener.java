package com.icontrol.openhomesimulator.camera.xmppclient;

public interface XmppClientIQListener {

    public boolean processResponseIQ(OpenHomeResponseIQ iq);

    public boolean processRequestIQ(OpenHomeRequestIQ iq);

    public void disconnected();
}
