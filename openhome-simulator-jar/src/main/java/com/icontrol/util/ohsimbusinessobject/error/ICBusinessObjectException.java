package com.icontrol.util.ohsimbusinessobject.error;


public class ICBusinessObjectException extends ICException {
    public ICBusinessObjectException(String message) {
        super(message);
    }

    public ICBusinessObjectException() {
        super();
    }

    public ICBusinessObjectException(Throwable cause) {
        super(cause);
    }

    public ICBusinessObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ICBusinessObjectException(ErrorCode code) {
        super(code);
    }

    public ICBusinessObjectException(ErrorCode code, String message) {
        super(code, message);
    }

    public ICBusinessObjectException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public ICBusinessObjectException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public void throwSelf() throws ICBusinessObjectException {
        throw this;
    }

    @Override
    public ICBusinessObjectException add(ErrorCode error) {
        if (errors.add(error)) {
            return this;
        }
        return null;
    }
}
