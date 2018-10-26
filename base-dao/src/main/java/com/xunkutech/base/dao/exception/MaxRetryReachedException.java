package com.xunkutech.base.dao.exception;

/**
 * Created by jason on 7/16/17.
 */
public class MaxRetryReachedException extends RollbackException {
    public MaxRetryReachedException(String msg) {
        super(msg);
    }

    public MaxRetryReachedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
