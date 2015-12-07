/*
 * Lambda.java
 *
 * Copyright (c) 2014  inQool a.s.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.inqool.dcap;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides helper methods for functional programming.
 *
 * @author Matus Zamborsky (inQool)
 */
@SuppressWarnings("unused")
public class Lambda {
    public static <T> Stream<T> stream(Collection<T> list) {
        return Optional.ofNullable(list)
                .orElse(Collections.emptyList())
                .stream();
    }

    public static <T> Stream<T> stream(Optional<T> optional) {
        return optional
                .map(Stream::of)
                .orElseGet(Stream::empty);
    }

    @SafeVarargs
    public static <T> Stream<T> concat(Stream<T> ... streams) {
        return Stream.of(streams)
                .parallel()
                .reduce(Stream::concat)
                .orElseGet(Stream::empty);
    }
}
