package com.icontrol.util.ohsimbusinessobject.error;


public class ICUserException extends ICBusinessObjectException {
    public ICUserException(String message) {
        super(message);
    }

    public ICUserException() {
        super();
    }

    public ICUserException(Throwable cause) {
        super(cause);
    }

    public ICUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ICUserException(ErrorCode code) {
        super(code);
    }

    public ICUserException(ErrorCode code, String message) {
        super(code, message);
    }

    public ICUserException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public ICUserException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public void throwSelf() throws ICUserException {
        throw this;
    }

    @Override
    public ICUserException add(ErrorCode error) {
        if (errors.add(error)) {
            return this;
        }
        return null;
    }
}
