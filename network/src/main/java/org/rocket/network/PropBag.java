package org.rocket.network;

import java.util.stream.Stream;

public interface PropBag {
    Stream<PropKey> getPresentPropKeys();

    <T> Prop<T> getProp(PropKey key);
    <T> MutProp<T> getMutProp(PropKey key);

    default int getNrPresentProps() {
        return (int) getPresentPropKeys().count();
    }
}
