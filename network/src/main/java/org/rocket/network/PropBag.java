package org.rocket.network;

import com.google.inject.Key;

import java.util.stream.Stream;

public interface PropBag {
    Stream<Key<?>> getPresentPropKeys();

    <T> Prop<T> getProp(Key<?> key);
    <T> MutProp<T> getMutProp(Key<?> key);

    default int getNrPresentProps() {
        return (int) getPresentPropKeys().count();
    }

    default boolean isPropPresent(Key<?> key) {
        return getPresentPropKeys().filter(key::equals).findAny().isPresent();
    }
}
