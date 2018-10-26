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

import com.xunkutech.base.app.component.ratelimit.RateLimited;
import com.xunkutech.base.app.component.ratelimit.options.exception.AmbiguousOptionsException;
import com.xunkutech.base.app.component.ratelimit.options.exception.OptionsException;
import org.aspectj.lang.JoinPoint;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple delegating resolver.
 * <p/>
 * Delegating resolver attempts to find first resolver that supports specific key and {@link RateLimited} annotation combination.
 *
 * @author franjozilic
 */
public class DelegatingOptionsResolver implements OptionsResolver {

    private class DisabledOptions implements Options {

        private String resolvedKey;

        public DisabledOptions(final String resolvedKey) {
            this.resolvedKey = resolvedKey;
        }

        @Override
        public boolean blocked() {
            return false;
        }

        @Override
        public boolean enabled() {
            return false;
        }

        @Override
        public OptionsInterval interval() {
            return null;
        }

        @Override
        public Long maxRequests() {
            return null;
        }

        @Override
        public String resolvedKey() {
            return resolvedKey;
        }

        @Override
        public OptionsRetry retry() {
            return null;
        }

        @Override
        public boolean retryEnabled() {
            return false;
        }

    }

    private final Collection<OptionsResolver> resolvers;

    private final boolean treatMissingAsDisabled;

    public DelegatingOptionsResolver(final Collection<OptionsResolver> resolvers, final boolean treatMissingAsDisabled) {
        this.resolvers = resolvers;
        this.treatMissingAsDisabled = treatMissingAsDisabled;
    }

    public DelegatingOptionsResolver(final Collection<OptionsResolver> resolvers) {
        this.resolvers = resolvers;
        this.treatMissingAsDisabled = false;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public Options resolve(final String key, final JoinPoint joinPoint) throws OptionsException {
        final List<OptionsResolver> selected = new ArrayList<>();

        for (final OptionsResolver resolver : resolvers) {
            if (resolver.supports(key)) {
                selected.add(resolver);
            }
        }

        if (!CollectionUtils.isEmpty(selected) && selected.size() > 1) {
            throw new AmbiguousOptionsException("Expected unique resolver, found " + selected.size());
        }

        if (CollectionUtils.isEmpty(selected)) {
            if (treatMissingAsDisabled) {
                return new DisabledOptions(key);
            } else {
                throw new AmbiguousOptionsException("No option resolver found, and treatMissingAsDisabled is false");
            }
        }

        return selected.get(0).resolve(key, joinPoint);
    }

    @Override
    public boolean supports(final String key) {
        return true;
    }
}
