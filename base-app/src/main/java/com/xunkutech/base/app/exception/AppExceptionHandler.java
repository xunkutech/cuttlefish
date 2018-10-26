package com.xunkutech.base.app.exception;


import com.xunkutech.base.app.component.ratelimit.exception.CallBlockedException;
import com.xunkutech.base.app.component.ratelimit.exception.RateLimitExceededException;
import com.xunkutech.base.app.component.ratelimit.exception.RateLimitException;
import com.xunkutech.base.app.context.AppContextHolder;
import com.xunkutech.base.model.JsonSerializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

/**
 * The error handler execution using a separated thread. Do not access any context and service here!!!
 */
@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(AppExceptionHandler.class);

    @Autowired
    private MessageSourceAccessor accessor;

    @ExceptionHandler(value = {AppException.class})
    protected ResponseEntity<Object> handleAppException(AppException ex, WebRequest request) {
        logger.debug("Caught app exception: {}", ex.getMessageKey());
        ResponseBody body = ResponseBody.builder()
                .code(ex.getCode())
                .timestamp(ex.getTimestamp())
                .payload(ex.getPayload())
                .message(accessor.getMessage(
                        ex.getMessageKey(),
                        ex.getArgs(),
                        ex.getMessage()))
                .build();
        logger.debug("Error Output:\n {}", body.printJson());
        return handleExceptionInternal(ex,
                body.toJson(),
                new HttpHeaders(), ex.getHttpStatus(), request);
    }

    @ExceptionHandler(value = {IllegalStateException.class, IllegalAccessException.class, NullPointerException.class})
    protected ResponseEntity<Object> handleInternalException(RuntimeException ex, WebRequest request) {
        logger.debug("Caught internal exception: {}", ex.getMessage());
        ex.printStackTrace();
        ResponseBody body = ResponseBody.builder()
                .code(500)
                .timestamp(Instant.now())
                .message(ex.getMessage())
                .build();
        logger.debug("Error Output:\n {}", body.printJson());
        return handleExceptionInternal(ex,
                body.toJson(),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {RateLimitException.class, RateLimitExceededException.class, CallBlockedException.class})
    protected ResponseEntity<Object> handleRateLimitException(RuntimeException ex, WebRequest request) {
        logger.debug("Caught rate limit exception: {}", ex.getMessage());
        ex.printStackTrace();
        System.out.println(LocaleContextHolder.getLocale());
        System.out.println(AppContextHolder.currentAppContext().getLocale());
        ResponseBody body = ResponseBody.builder()
                .code(403)
                .timestamp(Instant.now())
                .message(ex.getMessage())
                .build();
        logger.debug("Error Output:\n {}", body.printJson());
        return handleExceptionInternal(ex,
                body.toJson(),
                new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @Getter
    @Setter
    @Builder
    static class ResponseBody implements JsonSerializable {
        private Integer code;
        private Instant timestamp;
        private String message;
        private byte[] payload;
    }
}