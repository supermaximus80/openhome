package com.icontrol.util.ohsimbusinessobject.error;


public class ICPrefException extends ICBusinessObjectException {
    public ICPrefException(String message) {
        super(message);
    }

    public ICPrefException() {
        super();
    }

    public ICPrefException(Throwable cause) {
        super(cause);
    }

    public ICPrefException(String message, Throwable cause) {
        super(message, cause);
    }

    public ICPrefException(ErrorCode code) {
        super(code);
    }

    public ICPrefException(ErrorCode code, String message) {
        super(code, message);
    }

    public ICPrefException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public ICPrefException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public void throwSelf() throws ICPrefException {
        throw this;
    }

    @Override
    public ICPrefException add(ErrorCode error) {
        if (errors.add(error)) {
            return this;
        }
        return null;
    }
}
