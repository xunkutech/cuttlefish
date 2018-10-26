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

package com.xunkutech.base.app.component.ratelimit.options;

/**
 * Common interface used to describe {@link com.xunkutech.base.app.component.ratelimit.RateLimited} behaviour.
 * <p/>
 * Options are resolved using {@link OptionsResolver} implementations.
 *
 * @author franjozilic
 */
public interface Options {

    /**
     * Use to block calls on annotated method.
     * Not used with {@link com.xunkutech.base.app.component.ratelimit.RateLimited.Configuration#ANNOTATION}.
     * Can be used when using {@link com.xunkutech.base.app.component.ratelimit.RateLimited.Configuration#DATABASE} to bloack all requests without code redeploy.
     *
     * @return true if all requests to this call should be blocked, false otherwise
     */
    boolean blocked();

    /**
     * Use to enable/disable rate limiting on annotated call.
     * Not used with {@link com.xunkutech.base.app.component.ratelimit.RateLimited.Configuration#ANNOTATION}.
     * Can be used when using {@link com.xunkutech.base.app.component.ratelimit.RateLimited.Configuration#DATABASE} to disable rate limiting without code redeploy.
     *
     * @return true if rate limiting should be enabled for this call, false otherwise
     */
    boolean enabled();

    /**
     * Interval for rate limiting.
     * See {@link OptionsInterval}
     *
     * @return interval
     */
    OptionsInterval interval();

    /**
     * Maximum number of requests in time interval defined by {@link #interval()}
     *
     * @return maximum number of requests
     */
    Long maxRequests();

    /**
     * Resolved key for rate limiting.
     * Can be original value, or mapped value.
     * Depends on {@link OptionsResolver} implementation.
     *
     * @return resolved key, never {@code null}
     */
    String resolvedKey();

    /**
     * Retry options for annotated call
     * See {@link OptionsRetry}
     *
     * @return retry configuration, not null if {@link #retryEnabled()} is {@code true}
     */
    OptionsRetry retry();

    /**
     * Use ot enable/disable retrying when rate limit is reached.
     *
     * @return true if retry is enabled, false otherwise
     */
    boolean retryEnabled();

}
