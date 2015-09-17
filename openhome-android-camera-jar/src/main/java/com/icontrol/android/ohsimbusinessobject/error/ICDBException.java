package com.icontrol.android.ohsimbusinessobject.error;


public class ICDBException extends ICPersistenceException {
    public ICDBException(String message) {
        super(message);
    }

    public ICDBException() {
        super();
    }

    public ICDBException(Throwable cause) {
        super(cause);
    }

    public ICDBException(String message, Throwable cause) {
        super(message, cause);
    }

    public ICDBException(ErrorCode code) {
        super(code);
    }

    public ICDBException(ErrorCode code, String message) {
        super(code, message);
    }

    public ICDBException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public ICDBException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public void throwSelf() throws ICDBException {
        throw this;
    }

    @Override
    public ICDBException add(ErrorCode error) {
        if (errors.add(error)) {
            return this;
        }
        return null;
    }
}
