package org.rocket.network;

import org.rocket.Nullable;

import java.util.Optional;

/**
 * A Prop is an immutable delegation to another reference. Even if immutable, {@link #get()} might not be constant.
 * It kinda act like a C pointer.
 * @param <T>
 */
public interface Prop<T> {
    /**
     * Retrieve the owner of this prop.
     * @return never null
     */
    NetworkClient getOwner();

    /**
     * Retrieve the identifier of this prop.
     * @return never null
     */
    PropId getId();

    /**
     * Retrieve the underlying reference.
     * @return never null
     * @throws java.lang.NullPointerException
     */
    T get();

    /**
     * {@code true} if this prop holds a reference, {@code false} if not.
     */
    boolean isDefined();

    /**
     * Retrieve the underlying reference or null.
     */
    default @Nullable T orNull() {
        if (isDefined()) {
            return null;
        }
        return get();
    }

    /**
     * Retrieve the underlying reference and wrap it inside an {@link java.util.Optional}.
     */
    default Optional<T> orEmpty() {
        if (isDefined()) {
            return Optional.empty();
        }
        return Optional.of(get());
    }
}
