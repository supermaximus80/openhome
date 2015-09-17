package com.icontrol.util.ohsimbusinessobject.error;


public class ICEMailException extends ICException {
    public ICEMailException(String message) {
        super(message);
    }

    public ICEMailException() {
        super();
    }

    public ICEMailException(Throwable cause) {
        super(cause);
    }

    public ICEMailException(String message, Throwable cause) {
        super(message, cause);
    }

    public ICEMailException(ErrorCode code) {
        super(code);
    }

    public ICEMailException(ErrorCode code, String message) {
        super(code, message);
    }

    public ICEMailException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public ICEMailException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public void throwSelf() throws ICEMailException {
        throw this;
    }

    @Override
    public ICEMailException add(ErrorCode error) {
        if (errors.add(error)) {
            return this;
        }
        return null;
    }
}
