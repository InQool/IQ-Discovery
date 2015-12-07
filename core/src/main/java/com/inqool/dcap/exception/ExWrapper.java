/*
 * ExceptionWrapper.java
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

package com.inqool.dcap.exception;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Matus Zamborsky (inQool)
 */
@SuppressWarnings("unused")
public class ExWrapper {
    @FunctionalInterface
    public interface ThrowingPredicate<T> {
        public boolean test(T t) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        public R apply(T t) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<R> {
        public R get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }



    public static<T> Predicate<T> test(ThrowingPredicate<? super T> predicate) {
        return e -> {
            try { return predicate.test(e); }
            catch (RuntimeException ex) { throw ex; }
            catch (Exception ex) { throw new LambdaException(ex); }
        };
    }

    /**
     * Basically, takes function that throws checked exceptions and returns function that throws runtime exceptions instead.
     */
    public static<T, R> Function<T, R> apply(ThrowingFunction<? super T, ? extends R> predicate) {
        return e -> {
            try { return predicate.apply(e); }
            catch (RuntimeException ex) { throw ex; }
            catch (Exception ex) { throw new LambdaException(ex); }
        };
    }

    public static<R> Supplier<R> get(ThrowingSupplier<? extends R> predicate) {
        return () -> {
            try { return predicate.get(); }
            catch (RuntimeException ex) { throw ex; }
            catch (Exception ex) { throw new LambdaException(ex); }
        };
    }

    public static<T> Consumer<T> accept(ThrowingConsumer<T> b) {
        return e -> {
            try { b.accept(e); }
            catch (RuntimeException ex) { throw ex; }
            catch (Exception ex) { throw new LambdaException(ex); }
        };
    }




    @SuppressWarnings("unused")
    public static<T> void unwrap(Supplier<T> s) throws Throwable {
        try {
            s.get();
        } catch (LambdaException ex) {
            throw ex.getCause();
        }
    }
}
