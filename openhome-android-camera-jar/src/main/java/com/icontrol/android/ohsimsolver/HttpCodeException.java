package com.icontrol.android.ohsimsolver;

public class HttpCodeException extends Error {

    private final int code;
    private final Object body;
    private final String errorString;
    private final String errorParams[];

    public HttpCodeException(int code, String message) {
        this(code, message, null, null, null);
    }

    public HttpCodeException(int code, String message, Throwable cause) {
        this(code, message, cause, null, null);
    }

    public HttpCodeException(int code, String message, Throwable cause,
                             String errorString, String... errorParams) {
        this(code, message, null, cause, errorString, errorParams);
    }

    public HttpCodeException(int code, String message, Object body, Throwable cause,
                             String errorString, String... errorParams) {
        super(message, cause);
        this.body = body;
        this.code = code;
        this.errorString = errorString;
        this.errorParams = errorParams;
    }

    public String getErrorString() {
        if (errorString == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(errorString);
        sb.append(";");
        if (errorParams != null) {
            for (int i = 0; i < errorParams.length; i++) {
                sb.append(errorParams[i]);
                if (i < errorParams.length - 1)
                    sb.append(",");
            }
        }
        return sb.toString();
    }

    public int getCode() {
        return code;
    }

    public Object getBody() {
        return body;
    }
}
