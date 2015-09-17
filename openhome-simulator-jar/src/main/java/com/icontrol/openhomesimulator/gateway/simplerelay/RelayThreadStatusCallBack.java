package com.icontrol.openhomesimulator.gateway.simplerelay;

public interface RelayThreadStatusCallBack {

    boolean isLive();

    void setLastRead(long timems);

    void addBytesRead(int len);

    void close();
}
