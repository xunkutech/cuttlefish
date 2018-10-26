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

import java.util.concurrent.TimeUnit;

/**
 * Common interface used to describe time interval.
 * <p/>
 * Simplifies specifying of time interval.
 * Instead of always using {@link TimeUnit#MILLISECONDS} and then asking for number of milliseconds, specify correct unit and their number.
 * Rate limiting processor will convert to appropriate unit for limiting.
 *
 * @author franjozilic
 */
public interface OptionsInterval {

    /**
     * Interval, ie. number of {@link #unit()}
     *
     * @return time interval, never {@code null}
     */
    Long interval();

    /**
     * TimeUnit of this interval
     *
     * @return TimeUnit of this interval, never {@code null}
     */
    TimeUnit unit();

}
