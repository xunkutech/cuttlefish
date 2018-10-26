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
 */

package com.xunkutech.base.app.component.ratelimit.analytics;

import com.xunkutech.base.app.component.ratelimit.RateLimited;
import com.xunkutech.base.app.component.ratelimit.options.Options;
import org.aspectj.lang.JoinPoint;

public class NopRateLimitAnalytics implements RateLimitAnalytics {

    @Override
    public void blocked(final JoinPoint joinPoint, final RateLimited rateLimited, final String key, final Options options) {
    }

    @Override
    public void disabled(final JoinPoint joinPoint, final RateLimited rateLimited, final String key, final Options options) {
    }

    @Override
    public void exceeded(final JoinPoint joinPoint, final RateLimited rateLimited, final String key, final Options options, final Integer remainingCount) {
    }

    @Override
    public void retryInterrupted(final JoinPoint joinPoint, final RateLimited rateLimited, final String key, final Options options) {
    }

    @Override
    public void succeeded(final JoinPoint joinPoint, final RateLimited rateLimited, final String key, final Options options, final Integer remainingCount) {
    }

}
