package com.icontrol.util.ohsimbusinessobject.error;


public class ICGroupException extends ICBusinessObjectException {
    public ICGroupException(String message) {
        super(message);
    }

    public ICGroupException() {
        super();
    }

    public ICGroupException(Throwable cause) {
        super(cause);
    }

    public ICGroupException(String message, Throwable cause) {
        super(message, cause);
    }

    public ICGroupException(ErrorCode code) {
        super(code);
    }

    public ICGroupException(ErrorCode code, String message) {
        super(code, message);
    }

    public ICGroupException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public ICGroupException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public void throwSelf() throws ICGroupException {
        throw this;
    }

    @Override
    public ICGroupException add(ErrorCode error) {
        if (errors.add(error)) {
            return this;
        }
        return null;
    }
}
