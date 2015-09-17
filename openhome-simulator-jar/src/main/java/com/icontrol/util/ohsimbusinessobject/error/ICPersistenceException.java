package com.icontrol.util.ohsimbusinessobject.error;


public class ICPersistenceException extends ICException {
    public ICPersistenceException(String message) {
        super(message);
    }

    public ICPersistenceException() {
        super();
    }

    public ICPersistenceException(Throwable cause) {
        super(cause);
    }

    public ICPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ICPersistenceException(ErrorCode code) {
        super(code);
    }

    public ICPersistenceException(ErrorCode code, String message) {
        super(code, message);
    }

    public ICPersistenceException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public ICPersistenceException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public void throwSelf() throws ICPersistenceException {
        throw this;
    }

    @Override
    public ICPersistenceException add(ErrorCode error) {
        if (errors.add(error)) {
            return this;
        }
        return null;
    }
}
