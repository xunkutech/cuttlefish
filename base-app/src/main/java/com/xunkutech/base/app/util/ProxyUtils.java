package com.xunkutech.base.app.util;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

public class ProxyUtils {

    /**
     * Get the <em>target</em> object of the supplied {@code candidate} object.
     * <p>If the supplied {@code candidate} is a Spring
     * {@linkplain AopUtils#isAopProxy proxy}, the target of the proxy will
     * be returned; otherwise, the {@code candidate} will be returned
     * <em>as is</em>.
     *
     * @param candidate the instance to check (potentially a Spring AOP proxy;
     *                  never {@code null})
     * @return the target object or the {@code candidate} (never {@code null})
     * @throws IllegalStateException if an error occurs while unwrapping a proxy
     * @see Advised#getTargetSource()
     * @see #getUltimateTargetObject
     */
    @SuppressWarnings("unchecked")
    public static <T> T getTargetObject(Object candidate) {
        Assert.notNull(candidate, "Candidate must not be null");
        try {
            if (AopUtils.isAopProxy(candidate) && candidate instanceof Advised) {
                return (T) ((Advised) candidate).getTargetSource().getTarget();
            }
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to unwrap proxied object", ex);
        }
        return (T) candidate;
    }

    /**
     * Get the ultimate <em>target</em> object of the supplied {@code candidate}
     * object, unwrapping not only a top-level proxy but also any number of
     * nested proxies.
     * <p>If the supplied {@code candidate} is a Spring
     * {@linkplain AopUtils#isAopProxy proxy}, the ultimate target of all
     * nested proxies will be returned; otherwise, the {@code candidate}
     * will be returned <em>as is</em>.
     *
     * @param candidate the instance to check (potentially a Spring AOP proxy;
     *                  never {@code null})
     * @return the target object or the {@code candidate} (never {@code null})
     * @throws IllegalStateException if an error occurs while unwrapping a proxy
     * @see Advised#getTargetSource()
     * @see org.springframework.aop.framework.AopProxyUtils#ultimateTargetClass
     */
    @SuppressWarnings("unchecked")
    public static <T> T getUltimateTargetObject(Object candidate) {
        Assert.notNull(candidate, "Candidate must not be null");
        try {
            if (AopUtils.isAopProxy(candidate) && candidate instanceof Advised) {
                return (T) getUltimateTargetObject(((Advised) candidate).getTargetSource().getTarget());
            }
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to unwrap proxied object", ex);
        }
        return (T) candidate;
    }


    private static <T> T extractTargetObject(T proxied) {
        try {
            return (T) findSpringTargetSource(proxied).getTarget();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> TargetSource findSpringTargetSource(T proxied) {
        Method[] methods = proxied.getClass().getDeclaredMethods();
        Method targetSourceMethod = findTargetSourceMethod(methods);
        targetSourceMethod.setAccessible(true);
        try {
            return (TargetSource) targetSourceMethod.invoke(proxied);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Method findTargetSourceMethod(Method[] methods) {
        for (Method method : methods) {
            if (method.getName().endsWith("getTargetSource")) {
                return method;
            }
        }
        throw new IllegalStateException(
                "Could not find target source method on proxied object");
    }
}
