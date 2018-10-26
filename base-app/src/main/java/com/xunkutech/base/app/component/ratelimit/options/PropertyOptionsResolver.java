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

import com.xunkutech.base.app.component.ratelimit.options.exception.IllegalConfigurationException;
import com.xunkutech.base.app.component.ratelimit.options.exception.OptionsException;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * RateLimit options resolver that uses application properties to configure rate limiting.
 * <p>
 * This is the most basic production options resolver where rate limiting configuration is store within application property  source (See: {@link org.springframework.core.env.Environment}).
 * Configuration change requires application restart, but it is possible to change rate limit configuration without code modifications
 * <p>
 * You can use {@link com.xunkutech.base.app.component.ratelimit.options.DelegatingOptionsResolver} to chain more then one resolver and use {@link AnnotationOptionsResolver} as fall back resolver.
 * <p>
 * Default prefix is "rate.limited" see following snippet for example:
 * <pre>
 *  {@code
 *   rate.limited.limitingkey.enabled=true
 *   rate.limited.limitingkey.requests=5
 *   rate.limited.limitingkey.interval=10
 *   rate.limited.limitingkey.interval.unit=SECONDS
 *   rate.limited.limitingkey.retry.enabled=true
 *   rate.limited.limitingkey.retry.count=2
 *   rate.limited.limitingkey.retry.interval=1
 *   rate.limited.limitingkey.retry.interval.unit=MINUTES
 *  }
 * </pre>
 * <p>
 * This set of properties would generate limiting configuration with:
 * <ul>
 * <li>no more then <b>5</b> requests in <b>10 seconds</b></li>
 * <li>no more then <b>2</b> retries every <b>1 minute</b></li>
 * </ul>
 *
 * @author Franjo Zilic
 */
public class PropertyOptionsResolver implements OptionsResolver {

    private static final String DEFAULT_PROPERTY_PREFIX = "rate.limited";

    private final String propertyFormatEnabled;

    private final String propertyFormatRequests;

    private final String propertyFormatInterval;

    private final String propertyFormatIntervalUnit;

    private final String propertyFormatRetryCount;

    private final String propertyFormatRetryInterval;

    private final String propertyFormatRetryIntervalUnit;

    private final String propertyFormatRetryEnabled;

    private Environment environment;

    public PropertyOptionsResolver() {
        this(DEFAULT_PROPERTY_PREFIX);
    }

    public PropertyOptionsResolver(final String propertyPrefix) {
        Assert.hasText(propertyPrefix, "Property prefix must not be empty or blank");
        //    Assert.notNull(environment);
        propertyFormatEnabled = String.format("%s.%%s.enabled", propertyPrefix);
        propertyFormatRequests = String.format("%s.%%s.requests", propertyPrefix);
        propertyFormatInterval = String.format("%s.%%s.interval", propertyPrefix);
        propertyFormatIntervalUnit = String.format("%s.%%s.interval.unit", propertyPrefix);
        propertyFormatRetryEnabled = String.format("%s.%%s.retry.enabled", propertyPrefix);
        propertyFormatRetryCount = String.format("%s.%%s.retry.count", propertyPrefix);
        propertyFormatRetryInterval = String.format("%s.%%s.retry.interval", propertyPrefix);
        propertyFormatRetryIntervalUnit = String.format("%s.%%s.retry.interval.unit", propertyPrefix);
    }

    /**
     * {@link PropertyOptionsResolver} is not dynamic, configuration can't be changed during runtime
     *
     * @return {@code false}
     */
    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public Options resolve(final String key, final JoinPoint joinPoint) throws OptionsException {

        final Boolean enabled = getEnabledProperty(key);
        if (enabled == null) {
            throw new IllegalConfigurationException("Missing .enabled property for key '".concat(key).concat("'"));
        }

        // check if call is enabled
        // generating options for disabled calls is not efficient
        if (!enabled) {
            return InternalOptions.disabled(key);
        }

        final Long requests = getRequestsProperty(key);
        final Long interval = getIntervalProperty(key);

        if (requests == null || requests < 1L || interval == null || interval < 1L) {
            throw new IllegalConfigurationException("Invalid configuration for '".concat(key).concat("' .requests and .interval must be a positive number"));
        }

        final TimeUnit intervalUnit = getProperty(propertyFormatIntervalUnit, key, TimeUnit.class, TimeUnit.MINUTES);

        final InternalOptions options = InternalOptions.enabled(key, requests, InternalOptions.intervalOf(interval, intervalUnit));

        if (getProperty(propertyFormatRetryEnabled, key, Boolean.TYPE, false)) {
            final Integer retry = getProperty(propertyFormatRetryCount, key, Integer.class);
            final Long retryInterval = getProperty(propertyFormatRetryInterval, key, Long.class);
            final TimeUnit retryUnit = getProperty(propertyFormatRetryIntervalUnit, key, TimeUnit.class, TimeUnit.MINUTES);

            if (retry == null || retry < 1 || retryInterval == null || retryInterval < 1) {
                throw new IllegalConfigurationException("Invalid configuration for '".concat(key).concat("' .retry.count and .retry.interval must be a positive number"));
            }

            options.enableRetry(retry, InternalOptions.intervalOf(retryInterval, retryUnit));
        }

        return options;
    }

    private Boolean getEnabledProperty(final String key) {
        return getProperty(propertyFormatEnabled, key, Boolean.class);
    }

    private Long getRequestsProperty(final String key) {
        return getProperty(propertyFormatRequests, key, Long.class);
    }

    private Long getIntervalProperty(final String key) {
        return getProperty(propertyFormatInterval, key, Long.class);
    }

    private <T> T getProperty(final String propertyFormat, final String key, final Class<T> type, final T defaultValue) {
        return environment.getProperty(String.format(propertyFormat, key), type, defaultValue);
    }

    private <T> T getProperty(final String propertyFormat, final String key, final Class<T> type) {
        return environment.getProperty(String.format(propertyFormat, key), type);
    }

    /**
     * Attempts to locate configuration form application environment.
     *
     * @param key resolved key for rate limited operation
     * @return {@code true} if environment contains property {@code propertyPrefix.concat(key)} with value of key property
     */
    @Override
    public boolean supports(final String key) {
        if (!StringUtils.hasText(key)) {
            // this should not happen
            return false;
        }

        final Boolean enabled = getEnabledProperty(key);
        if (enabled == null) {
            return false;
        }

        // check if call is enabled
        // processing all options for disabled calls is not efficient
        if (!enabled) {
            return true;
        }

        final Long requestsProperty = getRequestsProperty(key);
        final Long intervalProperty = getIntervalProperty(key);
        return requestsProperty != null
                && requestsProperty > 0
                && intervalProperty != null
                && intervalProperty > 0;
    }

    @Autowired
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
}
