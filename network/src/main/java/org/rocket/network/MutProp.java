package org.rocket.network;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface MutProp<T> extends Prop<T> {
    void set(Optional<T> opt);

    default void set(T o) {
        set(Optional.of(o));
    }

    default void remove() {
        set(Optional.empty());
    }

    default T getOrSet(Supplier<T> fn) {
        Optional<T> opt = tryGet();
        if (opt.isPresent()) {
            return opt.get();
        }
        T o = fn.get();
        set(Optional.of(o));
        return o;
    }

    default Optional<T> getOrMaySet(Supplier<Optional<T>> fn) {
        Optional<T> opt = tryGet();
        if (opt.isPresent()) {
            return opt;
        }
        Optional<T> opt2 = fn.get();
        if (opt2.isPresent()) {
            set(opt2);
        }
        return opt2;
    }

    default MutProp<T> transform(UnaryOperator<T> fn) {
        set(fn.apply(get()));
        return this;
    }

    default MutProp<T> mayTransform(UnaryOperator<T> fn) {
        Optional<T> opt = tryGet();
        if (opt.isPresent()) {
            set(fn.apply(opt.get()));
        }
        return this;
    }
}
