package com.icontrol.android.ohsimbusinessobject.error;


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


//extends IOException only for backward compatibility
public class ICException extends IOException {
    protected List<ErrorCode> errors = new LinkedList<ErrorCode>();
    protected static final String[] EMPTY_ARGUMENTS = new String[0];
    protected List<String[]> arguments = new LinkedList<String[]>();

    protected static final boolean FILL_IN_STACK_TRACE = true;


    public ICException(String message) {
        super(message);
    }

    public ICException() {
        super("");
    }

    public ICException(Throwable cause) {
        this();
        initCause(cause);
    }

    public ICException(String message, Throwable cause) {
        this(message);
        initCause(cause);
    }

    public ICException(ErrorCode code) {
        this(code.getDescription());
        add(code);
        add(EMPTY_ARGUMENTS);
    }

    public ICException(ErrorCode code, String message) {
        this(message + " : " + code.getDescription());
        add(code);
        add(EMPTY_ARGUMENTS);
    }

    public ICException(ErrorCode code, Throwable cause) {
        this(code);
        initCause(cause);
    }

    public ICException(ErrorCode code, String message, Throwable cause) {
        this(code, message);
        initCause(cause);
    }

    public ICException(ErrorCode code, String[] arguments) {
        this(code.getDescription());
        add(code);
        add(arguments);
    }

    public ICException(ErrorCode code, String[] arguments, String message) {
        this(message + " : " + code.getDescription());
        add(code);
        add(arguments);
    }

    public ICException(ErrorCode code, String[] arguments, Throwable cause) {
        this(code.getDescription());
        add(code);
        add(arguments);
        initCause(cause);
    }

    public ICException(ErrorCode code, String[] arguments, String message, Throwable cause) {
        this(message);
        add(code);
        add(arguments);
        initCause(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (FILL_IN_STACK_TRACE) {
            return super.fillInStackTrace();
        }
        return this;
    }

    public ErrorCode firstErrorCode() {
        if (errors.size() == 0) {
            return null;
        }
        return errors.get(0);
    }

    @Override
    public Throwable initCause(Throwable cause) {
        if (cause instanceof ICException) {
            ICException exc = (ICException) cause;
            List<ErrorCode> errs = exc.errorCodes();
            List<String[]> args = exc.arguments();
            int argSize = args.size();
            for (int i = 0; i < errs.size(); i++) {
                ErrorCode ec = errs.get(i);
                String[] arg = EMPTY_ARGUMENTS;
                if (argSize > i) {
                    arg = args.get(i);
                }
                add(ec);
                add(arg);
            }
            exc.clearArguments();
            exc.clearErrors();
        }
        return super.initCause(cause);
    }

    protected void clearArguments() {
        arguments.clear();
    }

    protected void clearErrors() {
        errors.clear();
    }

    public String[] firstArguments() {
        if (arguments.size() == 0) {
            return EMPTY_ARGUMENTS;
        }
        return arguments.get(0);
    }


    protected List<ErrorCode> errorCodes() {
        return errors;
    }

    protected List<String[]> arguments() {
        return arguments;
    }

    public ICException add(ErrorCode error) {
        if (errors.add(error)) {
            return this;
        }
        return null;
    }

    public ICException add(String[] arguments) {
        if (arguments == null) {
            arguments = EMPTY_ARGUMENTS;
        }
        if (this.arguments.add(arguments)) {
            return this;
        }
        return null;
    }

    public void throwSelf() throws ICException {
        throw this;
    }
}
