package com.xunkutech.base.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Tell the bean that this is entityA locale variable field
 * <p>
 * Created by jason on 7/10/17.
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface LocaleVar {
}
