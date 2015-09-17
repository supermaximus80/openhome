package com.icontrol.openhomesimulator.gateway;

import java.util.Date;

public class MediaInstance {

    private Date date;
    private byte[] media;
    private String contentType;
    private String id;
    private boolean notified;

    public MediaInstance(Date date, byte[] media, String contentType, String id) {
        this.date = date;
        this.media = media;
        this.contentType = contentType;
        this.id = id;
        this.notified = false;
    }

    public Date getDate() {
        return date;
    }

    public byte[] getMedia() {
        return media;
    }

    public String getContentType() {
        return contentType;
    }

    public String getId() {
        return id;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

}
