package com.icontrol.android.openhomesimulator.simplerelay;

public interface RelayThreadStatusCallBack {

    boolean isLive();

    void setLastRead(long timems);

    void addBytesRead(int len);

    void close();
}
