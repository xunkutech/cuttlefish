package com.xunkutech.base.app.annotation;

import com.xunkutech.base.app.service.AppService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ServiceHandler {
    /**
     * SpEL expression to target service method.
     */
    String value() default "";

    Class<? extends AppService> serviceClass() default DEFAULT.class;

    boolean async() default true;

    class DEFAULT implements AppService<Object, Object> {
        @Override
        public Object handle(Object o) {
            return null;
        }
    }
}
