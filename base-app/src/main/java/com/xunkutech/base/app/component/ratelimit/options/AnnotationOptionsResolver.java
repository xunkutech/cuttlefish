/*
 * Copyright (c) 2017 Franjo Žilić <frenky666@gmail.com>
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

import com.xunkutech.base.app.component.ratelimit.RateLimited;
import com.xunkutech.base.app.component.ratelimit.RateLimitedRetry;
import com.xunkutech.base.app.component.ratelimit.options.exception.AmbiguousOptionsException;
import com.xunkutech.base.app.component.ratelimit.options.exception.IllegalConfigurationException;
import com.xunkutech.base.app.component.ratelimit.options.exception.OptionsException;
import org.aspectj.lang.JoinPoint;

import static com.xunkutech.base.app.component.ratelimit.util.JoinPointUtil.findAnnotation;

/**
 * {@link OptionsResolver} that uses {@link RateLimited} and {@link RateLimitedRetry} properties to configure rate limiting.
 * <p>
 * Dynamic changes are not possible when rate limiting is configured within code.
 * Use of {@link AnnotationOptionsResolver} on it's own is discouraged in production code,
 * since any change of rate limit configuration requires building and deploying entire application.
 * </p>
 * <p>
 * It is recommended to use any other {@link OptionsResolver} for production code,
 * or to chain {@link AnnotationOptionsResolver} as last, fall-back, resolver with {@link com.xunkutech.base.app.component.ratelimit.options.DelegatingOptionsResolver}.
 * </p>
 *
 * @author Franjo Zilic
 */
public class AnnotationOptionsResolver implements OptionsResolver {

    /**
     * {@link AnnotationOptionsResolver} is not dynamic, configuration can't be changed during runtime
     *
     * @return {@code false}
     */
    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public Options resolve(final String key, final JoinPoint joinPoint) throws OptionsException {

        // find most specific @RateLimited annotation for invoked join point
        final RateLimited rateLimited = findAnnotation(joinPoint, RateLimited.class);

        if (rateLimited == null) {
            // this should never happen
            // some kind of validation must be present
            throw new AmbiguousOptionsException("Unable to locate @RateLimited annotation");
        }

        // check if call is enabled
        // generating options for disabled calls is not efficient
        if (!rateLimited.enabled()) {
            return InternalOptions.disabled(key);
        }

        // validate configuration, default values are not valid since annotation based configuration is not recommended for any production use
        if (rateLimited.maxRequests() == -1 || rateLimited.interval().interval() == -1) {
            throw new IllegalConfigurationException("Annotation configuration is enabled, maxRequests and interval must be greater then 0");
        }

        final InternalOptions options = InternalOptions.enabled(key, rateLimited.maxRequests(), InternalOptions.intervalOf(rateLimited.interval().interval(), rateLimited.interval().unit()));

        // attempt to locate retry configuration
        final RateLimitedRetry retry = findAnnotation(joinPoint, RateLimitedRetry.class);

        if (retry != null) {

            if (retry.retryCount() < 1 && retry.interval().interval() < 1) {
                // in case of miss use
                throw new IllegalConfigurationException("Retry configuration is not valid");
            } else {
                options.enableRetry(retry.retryCount(), InternalOptions.intervalOf(retry.interval().interval(), retry.interval().unit()));
            }
        }

        return options;
    }

    /**
     * {@link AnnotationOptionsResolver} supports all keys by default
     *
     * @param key resolved key for rate limited operation
     * @return {@code true}
     */
    @Override
    public boolean supports(final String key) {
        return true;
    }
}
