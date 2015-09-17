package com.icontrol.ohcm;

/**
 * @author rbitonio
 */

@SuppressWarnings("serial")
public class CameraException extends Exception {

    private String commandName;
    private int httpCode = -1;
    private String httpResponse;

    public CameraException(String commandName, int httpCode, String httpResponse, String message, Throwable cause) {
        super(message, cause);
        this.commandName = commandName;
        this.httpCode = httpCode;
        this.httpResponse = httpResponse;
    }

    public CameraException(String commandName, int httpCode, String httpResponse, String message) {
        this(commandName, httpCode, httpResponse, message, null);
    }

    public CameraException(int httpCode, String httpResponse, String message) {
        this(null, httpCode, httpResponse, message, null);
    }

    public CameraException(int httpCode, String httpResponse, String message, Throwable cause) {
        this(null, httpCode, httpResponse, message, cause);
    }

    public CameraException(String message) {
        super(message);
    }

    public CameraException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCommandName() {
        return this.commandName;
    }

    public int getHttpCode() {
        return this.httpCode;
    }

    public String getHttpResponse() {
        return this.httpResponse;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Command: (" + commandName);
        if (httpCode != -1) {
            sb.append(") Code: (" + httpCode);
        }
        if (httpResponse != null) {
            sb.append(") Response: (" + httpResponse);
        }
        String message = getMessage();
        if (message != null) {
            sb.append(") Message: (" + message);
        }
        sb.append(")");
        return sb.toString();
    }
}
