package com.xunkutech.base.app.exception;

import org.springframework.http.HttpStatus;

public interface AppExceptionInventory {

    Integer getCode();

    HttpStatus getHttpStatus();

    default AppException.AppExceptionBuilder builder() {
        return AppException.builder()
                .messageKey(getClass().getName() + "." + toString())
                .code(getCode())
                .httpStatus(getHttpStatus());
    }

    default AppException fire() {
        throw builder().build();
    }

    default AppException fire(Object... args) {
        throw builder().args(args).build();
    }

    default AppException fire(byte[] payload) {
        throw builder().payload(payload).build();
    }

    default AppException fire(byte[] payload, Object... args) {
        throw builder().payload(payload).args(args).build();
    }
}