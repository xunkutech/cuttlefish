package com.xunkutech.base.app.component;

import com.xunkutech.base.app.exception.AppExceptionInventory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ComponentException implements AppExceptionInventory {
    FILE_DUPLICATED(100, HttpStatus.BAD_REQUEST),
    FILE_SAVE_ERROR(101, HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_ERROR(102, HttpStatus.BAD_REQUEST);

    private Integer code;
    private HttpStatus httpStatus;

    @Override
    public Integer getCode() {
        return null;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return null;
    }
}
