package org.rocket;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class MoreCollectors {
    public static <T> Collector<T, ?, T> uniq() {
        return new Collector<T, AtomicReference<T>, T>() {
            @Override
            public Supplier<AtomicReference<T>> supplier() {
                return AtomicReference::new;
            }

            @Override
            public BiConsumer<AtomicReference<T>, T> accumulator() {
                return (ref, val) -> {
                    if (ref.get() != null) {
                        throw new IllegalStateException("not unique");
                    }
                    ref.set(val);
                };
            }

            @Override
            public BinaryOperator<AtomicReference<T>> combiner() {
                return (left, right) -> {
                    if (left.get() != null && right.get() != null) {
                        throw new IllegalStateException("not unique");
                    }
                    if (right.get() != null) {
                        return right;
                    }
                    return left;
                };
            }

            @Override
            public Function<AtomicReference<T>, T> finisher() {
                return AtomicReference::get;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.noneOf(Characteristics.class);
            }
        };
    }
}
