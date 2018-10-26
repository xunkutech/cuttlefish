/*
 * Copyright (c) 2015 Franjo Žilić <frenky666@gmail.com>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

package com.xunkutech.base.app.component.ratelimit.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class JoinPointUtil {

    private static final Logger log = LoggerFactory.getLogger(JoinPointUtil.class);

    /**
     * Attempts to locate annotation of {@param type}.
     * <p/>
     * First on invoked method, then on class.
     *
     * @param joinPoint join point of around aspect
     * @return {@param type} annotation instance
     */
    public static <A extends Annotation> A findAnnotation(final JoinPoint joinPoint, final Class<A> type) {
        A annotation = null;

        try {
            // attempt to find annotation on method
            final Method method = method(joinPoint);
            annotation = AnnotationUtils.findAnnotation(method, type);

            if (annotation == null) {
                // no annotation on method, try class level
                annotation = AnnotationUtils.findAnnotation(type(joinPoint), type);
            }
        } catch (RuntimeException exception) {
            log.warn("Failed to find annotation for method annotated method", exception);
        }

        return annotation;
    }

    public static Method method(final JoinPoint joinPoint) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return ClassUtils.getMostSpecificMethod(signature.getMethod(), type(joinPoint));
    }

    public static Class<?> type(final JoinPoint joinPoint) {
        return joinPoint.getTarget().getClass();
    }

    public static String methodName(final JoinPoint joinPoint) {
        final Method method = method(joinPoint);
        if (method != null) {
            return method.getName();
        }
        return null;
    }

    public static String typeName(final JoinPoint joinPoint) {
        final Class<?> type = type(joinPoint);

        if (type != null) {
            return type.getName();
        }
        return null;
    }

    private JoinPointUtil() {
    }

}
