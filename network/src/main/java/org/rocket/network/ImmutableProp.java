package org.rocket.network;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class ImmutableProp<T> implements Prop<T> {
    private final Optional<T> opt;

    public ImmutableProp(Optional<T> opt) {
        this.opt = requireNonNull(opt, "opt");
    }

    @Override
    public Optional<T> tryGet() {
        return opt;
    }

    @Override
    public <R> Prop<R> map(Function<T, R> fn) {
        return new ImmutableProp<>(opt.map(fn));
    }
}
