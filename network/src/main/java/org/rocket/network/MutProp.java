package org.rocket.network;

import org.rocket.Nullable;

import java.util.function.Supplier;

/**
 * A MutProp is a mutable delegation to another reference.
 * @param <T>
 */
public interface MutProp<T> extends Prop<T> {
    /**
     * Assign the underlying reference to this prop. It calls {@link #forget()} if the parameter is {@code null}.
     * @param param a nullable reference
     */
    void set(@Nullable T param);

    /**
     * Forget the underlying reference.
     */
    void forget();

    /**
     * Helper method.
     */
    default Prop<T> readonly() {
        return this;
    }

    /**
     * Retrieve the underlying reference or assign it if not defined.
     * @param fn a lazy getter
     * @return the underlying reference
     */
    default T getOrSet(Supplier<T> fn) {
        if (!isDefined()) {
            set(fn.get());
        }
        return get();
    }
}
