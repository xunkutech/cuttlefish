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
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import static com.xunkutech.base.app.component.ratelimit.util.JoinPointUtil.method;
import static com.xunkutech.base.app.component.ratelimit.util.JoinPointUtil.type;

/**
 * Generated rate limiting key.
 * <p/>
 * Resolution follows:
 * - If {@link RateLimited#key()} is not empty then this value.
 * - If {@link RateLimited#keyExpression()} is not empty then generate.
 * - If neither is present then key will be generated as {@code className + methodName}
 * <p/>
 * When generating key from expression SpEL context has following variables
 * - type - fully qualified class name
 * - method - method name for invoked method
 * - p0..pN - positional arguments of invoked method
 *
 * @author franjozilic
 */
public class DefaultKeyGenerator implements KeyGenerator {

    @Override
    public String key(final String key, final String keyExpression, final JoinPoint joinPoint) {
        if (StringUtils.hasText(key)) {
            return key;
        }

        if (StringUtils.hasText(keyExpression)) {
            final StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("type", type(joinPoint).getName());
            context.setVariable("method", method(joinPoint).getName());

            final Object[] args = joinPoint.getArgs();
            if (args != null) {
                for (int idx = 0; idx < args.length; idx++) {
                    context.setVariable(String.format("p%d", idx), args[idx]);
                }
            }

            final SpelExpressionParser parser = new SpelExpressionParser();
            final Expression sExpression = parser.parseExpression(keyExpression);

            return sExpression.getValue(context, String.class);
        }

        return type(joinPoint).getName() + "." + method(joinPoint).getName();
    }
}
