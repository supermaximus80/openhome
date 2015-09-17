package com.icontrol.util.ohsimbusinessobject.error;


public class ICNetworkException extends ICBusinessObjectException {
    public ICNetworkException(String message) {
        super(message);
    }

    public ICNetworkException() {
        super();
    }

    public ICNetworkException(Throwable cause) {
        super(cause);
    }

    public ICNetworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public ICNetworkException(ErrorCode code) {
        super(code);
    }

    public ICNetworkException(ErrorCode code, String message) {
        super(code, message);
    }

    public ICNetworkException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public ICNetworkException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public void throwSelf() throws ICNetworkException {
        throw this;
    }

    @Override
    public ICNetworkException add(ErrorCode error) {
        if (errors.add(error)) {
            return this;
        }
        return null;
    }
}
