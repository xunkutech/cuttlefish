package com.xunkutech.base.dao.exception;

/**
 * Created by jason on 7/16/17.
 */
public class DataCorruptedException extends RollbackException {
    public DataCorruptedException(String msg) {
        super(msg);
    }

    public DataCorruptedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
