package org.rocket.network;

import java.util.Optional;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class ConcurrentMutProp<T> implements MutProp<T> {
    private Optional<T> opt;
    private final StampedLock lock = new StampedLock();

    private Optional<T> read() {
        Optional<T> res;
        long stamp = lock.tryOptimisticRead();
        res = opt;
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                res = opt;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return res;
    }

    private void write(Optional<T> opt) {
        long stamp = lock.writeLock();
        try {
            this.opt = opt;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public Optional<T> tryGet() {
        return read();
    }

    @Override
    public <R> Prop<R> map(Function<T, R> fn) {
        return new ImmutableProp<>(read().map(fn));
    }

    @Override
    public void set(Optional<T> opt) {
        write(requireNonNull(opt, "opt"));
    }
}
