package com.xunkutech.base.dao.exception;

import org.springframework.dao.DataAccessException;

/**
 * Created by jason on 10/10/15.
 */
public abstract class RollbackException extends DataAccessException {

    private static final long serialVersionUID = 3465267554981735077L;

    public RollbackException(String msg) {
        super(msg);
    }

    public RollbackException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
