package com.xunkutech.base.app.util;


import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;

public class OkHttpResponseFuture implements okhttp3.Callback {
    private static Logger logger = LoggerFactory.getLogger(OkHttpResponseFuture.class);
    public final CompletableFuture<Response> future = new CompletableFuture<>();

    int maxRetry = 0;
    OkHttpClient client;

    int retryTimes = 0;

    public OkHttpResponseFuture(OkHttpClient client, int maxRetry) {
        this.maxRetry = maxRetry;
        this.client = client;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        if (e.getClass().equals(SocketTimeoutException.class)
                && (retryTimes < maxRetry)) {
            retryTimes++;
            logger.debug("Socket timeout retry: {}", retryTimes);
            client.newCall(call.request()).enqueue(this);
        }
        logger.error("Connection error: {}", e.getMessage());
        future.completeExceptionally(e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        future.complete(response);
    }
}