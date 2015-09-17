package com.icontrol.ohsimsolver;

import com.icontrol.openhome.data.*;


public class ResponseStatusFactory {

    // O=1-OK, 2-Device Busy, 3-Device Error, 4-Invalid Operation, 5-Invalid XML Format, 6-Invalid XML Content; 7-Reboot Required
    static public enum STATUSCODE {NO_ERROR, OK, DEVICE_BUSY, DEVICE_ERROR, INVALID_OPERATION, INVALID_XML_FORMAT, INVALID_XML_CONTENT, REBOOT_REQUIRED}



    static public ResponseStatus getResponseOK() {
        ResponseStatus response = new ResponseStatus();
        response.setStatusCode(STATUSCODE.OK.ordinal());
        response.setStatusString(STATUSCODE.OK.name());
        return response;
    }

    static public ResponseStatus getResponseError(STATUSCODE code) {
        ResponseStatus response = new ResponseStatus();
        response.setStatusCode(code.ordinal());
        response.setStatusString(code.name());
        return response;
    }
}
