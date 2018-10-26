package com.xunkutech.base.app.exception;

import com.xunkutech.base.model.JsonSerializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Locale;

@Getter
@Setter
@Builder
public class AppException extends RuntimeException implements JsonSerializable {

    private Instant timestamp = Instant.now();
    private HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    private String messageKey;
    private Integer code;
    private Object[] args;
    private String message;
    private byte[] payload;

}
