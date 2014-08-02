package org.rocket.network;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HashPropBag implements PropBag {

    private final Map<PropKey, DefaultMutProp<?>> props;

    public HashPropBag() {
        props = Maps.newHashMap();
    }

    public HashPropBag(HashPropBag other) {
        props = Maps.newHashMap(other.props);
    }

    @SuppressWarnings("unchecked")
    private <T> DefaultMutProp<T> read(PropKey key) {
        return (DefaultMutProp<T>) props.computeIfAbsent(key, x -> new DefaultMutProp<>(Optional.empty()));
    }

    @Override
    public <T> Prop<T> getProp(PropKey key) {
        return read(key);
    }

    @Override
    public <T> MutProp<T> getMutProp(PropKey key) {
        return read(key);
    }

    @Override
    public Stream<PropKey> getPresentPropKeys() {
        return props.keySet().stream();
    }

    @Override
    public int getNrPresentProps() {
        return props.size();
    }

    @Override
    public String toString() {
        return getPresentPropKeys()
                .map(PropKey::toString)
                .collect(Collectors.joining(", "));
    }
}
