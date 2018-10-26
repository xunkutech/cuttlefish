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

package com.xunkutech.base.app.component.ratelimit.key;

import com.xunkutech.base.app.component.ratelimit.RateLimited;
import org.aspectj.lang.JoinPoint;

/**
 * Used to generate key for rate limited operation
 * <p/>
 *
 * @author franjozilic
 */
public interface KeyGenerator {

    /**
     * Generated or resolved key
     *
     * @param key           key from {@link RateLimited#key()}
     * @param keyExpression key expression from {@link RateLimited#keyExpression()}
     * @param joinPoint     join point of advised method
     * @return generated or resolved key, never null or empty
     */
    String key(final String key, final String keyExpression, JoinPoint joinPoint);

}
