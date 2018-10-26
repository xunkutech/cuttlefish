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

package com.xunkutech.base.app.component.ratelimit;

import com.xunkutech.base.app.component.ratelimit.aspect.RateLimitingAdvice;
import com.xunkutech.base.app.component.ratelimit.checker.RateChecker;
import com.xunkutech.base.app.component.ratelimit.checker.RedisRateChecker;
import com.xunkutech.base.app.component.ratelimit.key.DefaultKeyGenerator;
import com.xunkutech.base.app.component.ratelimit.key.KeyGenerator;
import com.xunkutech.base.app.component.ratelimit.options.AnnotationOptionsResolver;
import com.xunkutech.base.app.component.ratelimit.options.OptionsResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Franjo Zilic
 */
@Configuration
public class RateLimitConfiguration {

    @Bean
    public KeyGenerator keyGenerator() {
        return new DefaultKeyGenerator();
    }

    @Bean
    public OptionsResolver optionsResolver() {
        return new AnnotationOptionsResolver();
    }

    @Bean
    public RateLimitingAdvice rateLimitingAdvice(final KeyGenerator keyGenerator, final OptionsResolver optionsResolver, final RateChecker rateChecker) {
        return new RateLimitingAdvice(keyGenerator, optionsResolver, rateChecker);
    }

    @Bean
    public RateChecker redisTokenBucketRateChecker(final RedisOperations<String, String> redisOperations) {
        return new RedisRateChecker(redisOperations);
    }

    @Bean
    public StringRedisTemplate redisTemplate(final RedisConnectionFactory connectionFactory) {
        final StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setDefaultSerializer(new GenericToStringSerializer<>(Object.class));
        redisTemplate.setExposeConnection(true);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public JedisConnectionFactory connectionFactory() {
        final JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName("localhost");
        jedisConnectionFactory.setPort(6379);
        jedisConnectionFactory.setDatabase(0);
        return jedisConnectionFactory;
    }

}
