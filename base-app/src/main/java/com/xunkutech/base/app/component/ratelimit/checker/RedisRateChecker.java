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

package com.xunkutech.base.app.component.ratelimit.checker;

import com.xunkutech.base.app.component.ratelimit.options.OptionsInterval;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Rate limit checker implemented using Redis.<br>
 * Designed for use in distributed systems with no inter node synchronization.
 * <p/>
 * Implementation is based on algorithm described by <a href="https://engineering.classdojo.com/blog/2015/02/06/rolling-rate-limiter/">https://engineering.classdojo.com/blog/2015/02/06/rolling-rate-limiter/</a><br>
 * This implementation doesn't include checking maximum time between two requests, and is more relaxed then original.
 * <p/>
 * There is a possible race condition, but only downside is that some executions will be blocked.
 * Use retrying strategy to mitigate.
 *
 * @author franjozilic
 */
public class RedisRateChecker implements RateChecker {

    protected static class Callback implements SessionCallback<Boolean> {

        private final String key;

        private final Long maxRequests;

        private final OptionsInterval interval;

        private final String requestId;

        public Callback(final String key, final Long maxRequests, final OptionsInterval interval) {
            this.key = key;
            this.maxRequests = maxRequests;
            this.interval = interval;
            this.requestId = UUID.randomUUID().toString();
        }

        public Callback(final String key, final String requestId, final Long maxRequests, final OptionsInterval interval) {
            this.key = key;
            this.maxRequests = maxRequests;
            this.interval = interval;
            this.requestId = requestId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <K, V> Boolean execute(final RedisOperations<K, V> redisOperations) throws DataAccessException {
            return executeInternal((RedisOperations<String, String>) redisOperations);
        }

        private Boolean executeInternal(final RedisOperations<String, String> redisOperations) {
            redisOperations.multi();
            final long milliseconds = System.currentTimeMillis();
            final String callKey = requestId.concat("-").concat(Long.toString(milliseconds));

            // remove any older then one interval
            redisOperations.opsForZSet().removeRangeByScore(key, Double.MIN_VALUE, milliseconds - TimeUnit.MILLISECONDS.convert(interval.interval(), interval.unit()));

            // add current request (milliseconds should be sufficient, but add UUID)
            redisOperations.opsForZSet().add(key, callKey, milliseconds);

            // set expire for entire set, save memory
            redisOperations.expire(key, interval.interval(), interval.unit());

            // count total remaining
            redisOperations.opsForZSet().count(key, Double.MIN_VALUE, Double.MAX_VALUE);

            final List<Object> result = redisOperations.exec();

            // we made four calls during MULTI, expect that result size
            // last result hast be count and Long
            if (CollectionUtils.isEmpty(result) || result.size() != 4 || !(result.get(3) instanceof Long)) {
                return false;
            }
            final Long count = (Long) result.get(3);

            // more then we can handle, remove ours, call never happened
            // possible race condition here
            // another request might get blocked as well
            // this is better then blocking every other request if burst is too large
            if (count > maxRequests) {
                redisOperations.opsForZSet().remove(key, callKey);
                return false;
            }

            // limit has not been reached
            return true;
        }
    }

    private final RedisOperations<String, String> redisOperations;

    public RedisRateChecker(final RedisOperations<String, String> redisOperations) {
        this.redisOperations = redisOperations;
    }

    @Override
    public boolean check(final String key, final Long maxRequests, final OptionsInterval interval) {
        final Boolean execute = redisOperations.execute(new Callback(key, maxRequests, interval));
        if (execute == null) {
            return false;
        }
        return execute;
    }

}
