package com.xunkutech.base.app.service;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface AsyncFunction<O, S> {

    CompletableFuture<Output> service();

    class Output<O> {
       public O data;
    }
}
