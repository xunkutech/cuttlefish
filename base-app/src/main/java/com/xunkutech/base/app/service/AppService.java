package com.xunkutech.base.app.service;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface AppService<T, R> {

    /**
     * Execute this service to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R handle(T t);

    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies this function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V>    the type of input to the {@code before} function, and to the
     *               composed function
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before}
     * function and then applies this function
     * @throws NullPointerException if before is null
     * @see #andThen(AppService)
     */
    default <V> AppService<V, R> compose(AppService<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> handle(before.handle(v));
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V>   the type of output of the {@code after} function, and of the
     *              composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     * @see #compose(AppService)
     */
    default <V> AppService<T, V> andThen(AppService<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.handle(handle(t));
    }

}
