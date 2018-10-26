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

package com.xunkutech.base.app.component.ratelimit.aspect;

import com.xunkutech.base.app.component.ratelimit.RateLimited;
import com.xunkutech.base.app.component.ratelimit.analytics.NopRateLimitAnalytics;
import com.xunkutech.base.app.component.ratelimit.analytics.RateLimitAnalytics;
import com.xunkutech.base.app.component.ratelimit.checker.RateChecker;
import com.xunkutech.base.app.component.ratelimit.exception.CallBlockedException;
import com.xunkutech.base.app.component.ratelimit.exception.RateLimitExceededException;
import com.xunkutech.base.app.component.ratelimit.key.KeyGenerator;
import com.xunkutech.base.app.component.ratelimit.options.Options;
import com.xunkutech.base.app.component.ratelimit.options.OptionsResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xunkutech.base.app.component.ratelimit.util.JoinPointUtil.*;

@Aspect
public class RateLimitingAdvice {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingAdvice.class);

    private final OptionsResolver configurationResolver;

    private final KeyGenerator keyGenerator;

    private final RateChecker rateChecker;

    private final RateLimitAnalytics analytics;

    public RateLimitingAdvice(final KeyGenerator keyGenerator, final OptionsResolver configurationResolver, final RateChecker rateChecker) {
        this.configurationResolver = configurationResolver;
        this.keyGenerator = keyGenerator;
        this.rateChecker = rateChecker;
        this.analytics = new NopRateLimitAnalytics();
    }

    public RateLimitingAdvice(final KeyGenerator keyGenerator, final OptionsResolver configurationResolver, final RateChecker rateChecker, final RateLimitAnalytics analytics) {
        this.configurationResolver = configurationResolver;
        this.keyGenerator = keyGenerator;
        this.rateChecker = rateChecker;
        this.analytics = analytics;
    }

    @Around("@annotation(com.xunkutech.base.app.component.ratelimit.RateLimited) || @within(com.xunkutech.base.app.component.ratelimit.RateLimited)")
    public Object rateLimit(final ProceedingJoinPoint joinPoint) throws Throwable {
        log.trace("@RateLimited attempting to execute method: {}.{}", typeName(joinPoint), methodName(joinPoint));

        final RateLimited rateLimited = findAnnotation(joinPoint, RateLimited.class);
        final String key = keyGenerator.key(rateLimited.key(), rateLimited.keyExpression(), joinPoint);
        final Options options = configurationResolver.resolve(key, joinPoint);

        if (options.blocked()) {
            log.info("@RateLimited method {}.{} execution is blocked.", typeName(joinPoint), methodName(joinPoint));
            analytics.blocked(joinPoint, rateLimited, key, options);
            throw new CallBlockedException("Execution is blocked by configuration");
        }

        // skip disabled limiters
        if (options.enabled()) {

            Boolean canExecute;
            Integer retryCount = options.retryEnabled() ? options.retry().retryCount() + 1 : 1;
            do {
                canExecute = rateChecker.check(options.resolvedKey(), options.maxRequests(), options.interval());

                if (!canExecute && options.retryEnabled()) {
                    log.trace("@RateLimited rate exceeded for method {}.{} retry enabled, retrying for {}", typeName(joinPoint),
                            methodName(joinPoint), retryCount);
                    try {
                        Thread.sleep(options.retry().retryInterval().unit().toMillis(options.retry().retryInterval().interval()));
                    } catch (final InterruptedException exception) {
                        log.error("@RateLimited execution retry was interrupted", exception);
                        analytics.retryInterrupted(joinPoint, rateLimited, key, options);
                        throw new RateLimitExceededException("Interrupted while retrying", exception);
                    }
                }
            } while (!canExecute && --retryCount > 0);

            if (!canExecute) {
                log.warn("@RateLimited rate exceeded for method {}.{}, tires {}", typeName(joinPoint),
                        methodName(joinPoint), options.retryEnabled() ? options.retry().retryCount() + 1 : 1);
                analytics.exceeded(joinPoint, rateLimited, key, options, options.retryEnabled() ? options.retry().retryCount() + 1 : 1 - retryCount);
                throw new RateLimitExceededException("Rate limit has been exceeded");
            }

            analytics.succeeded(joinPoint, rateLimited, key, options, options.retryEnabled() ? options.retry().retryCount() + 1 : 1 - retryCount);
        } else {
            log.info("@RateLimited method {}.{} execution is disabled.", typeName(joinPoint), methodName(joinPoint));
            analytics.disabled(joinPoint, rateLimited, key, options);
        }

        return joinPoint.proceed();
    }

}
