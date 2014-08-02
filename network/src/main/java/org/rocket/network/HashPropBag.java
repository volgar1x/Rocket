package org.rocket.network;

import com.google.common.collect.Maps;
import com.google.inject.Key;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HashPropBag implements PropBag {

    private final Map<Key<?>, DefaultMutProp<?>> props;

    public HashPropBag() {
        props = Maps.newHashMap();
    }

    public HashPropBag(HashPropBag other) {
        props = Maps.newHashMap(other.props);
    }

    @SuppressWarnings("unchecked")
    private <T> DefaultMutProp<T> read(Key<?> key) {
        return (DefaultMutProp<T>) props.computeIfAbsent(key, x -> new DefaultMutProp<>(Optional.empty()));
    }

    @Override
    public <T> Prop<T> getProp(Key<?> key) {
        return read(key);
    }

    @Override
    public <T> MutProp<T> getMutProp(Key<?> key) {
        return read(key);
    }

    @Override
    public Stream<Key<?>> getPresentPropKeys() {
        return props.keySet().stream();
    }

    @Override
    public int getNrPresentProps() {
        return props.size();
    }

    @Override
    public String toString() {
        return getPresentPropKeys()
                .map(Key::toString)
                .collect(Collectors.joining(", "));
    }
}
