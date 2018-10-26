package com.xunkutech.base.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Tell deepCopy don't override the immutable field.
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Immutable {
}
