package com.xunkutech.base.dao.exception;

public class DataNotExistException extends RollbackException {
    public DataNotExistException(String msg) {
        super(msg);
    }

    public DataNotExistException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
