package com.xunkutech.base.model.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class FunctionUtils {

    public static <T, E extends Exception> Consumer<T> wrapConsumer(ThrowingConsumer<T, E> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception ignore) {
            }
        };
    }

    public static <T, R, E extends Exception> Function<T, R> wrapFunction(ThrowingFunction<T, R, E> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception ignore) {
            }
            return null;
        };
    }

    public static <T, E extends Exception> Predicate<T> wrapPredicate(ThrowingPredicate<T, E> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Exception ignore) {
            }
            return false;
        };
    }

    @FunctionalInterface
    interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }

    @FunctionalInterface
    interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    @FunctionalInterface
    interface ThrowingPredicate<T, E extends Exception> {
        boolean test(T t) throws E;
    }
}
