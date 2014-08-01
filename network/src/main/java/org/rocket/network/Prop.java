package org.rocket.network;

import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Prop<T> {
    Optional<T> tryGet();

    <R> Prop<R> map(Function<T, R> fn);

    default T get() {
        return tryGet().get();
    }

    default T validate(Predicate<T> fn) {
        T o = get();
        if (!fn.test(o)) {
            throw new IllegalStateException();
        }
        return o;
    }

    default T validate(Predicate<T> fn, String msg, Object... args) {
        T o = get();
        if (!fn.test(o)) {
            throw new IllegalStateException(String.format(msg, args));
        }
        return o;
    }

    default Future<T> asFuture() {
        Optional<T> opt = tryGet();
        if (opt.isPresent()) {
            return Futures.success(opt.get());
        }
        return Future.never();
    }
}
