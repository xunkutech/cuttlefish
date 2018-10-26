package com.xunkutech.base.dao.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class DataDuplicatedException extends DataIntegrityViolationException {
    public DataDuplicatedException(String msg) {
        super(msg);
    }

    public DataDuplicatedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
