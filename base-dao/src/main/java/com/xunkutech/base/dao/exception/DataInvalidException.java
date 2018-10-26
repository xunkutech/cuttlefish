package com.xunkutech.base.dao.exception;

public class DataInvalidException extends RollbackException {
    public DataInvalidException(String msg) {
        super(msg);
    }

    public DataInvalidException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
