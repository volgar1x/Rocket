package org.rocket.network;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class DefaultMutProp<T> implements MutProp<T> {
    private Optional<T> opt;

    public DefaultMutProp(Optional<T> opt) {
        this.opt = requireNonNull(opt, "opt");
    }

    @Override
    public Optional<T> tryGet() {
        return opt;
    }

    @Override
    public <R> Prop<R> map(Function<T, R> fn) {
        return new DefaultMutProp<>(opt.map(fn));
    }

    @Override
    public void set(Optional<T> opt) {
        this.opt = requireNonNull(opt, "opt");
    }
}
