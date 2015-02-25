package org.rocket.network.netty;

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.rocket.Nullable;
import org.rocket.network.MutProp;
import org.rocket.network.NetworkClient;
import org.rocket.network.Prop;
import org.rocket.network.PropId;

import java.util.Optional;

final class NettyMutProp<T> implements MutProp<T> {
    public static <T> AttributeKey<T> asAttributeKey(PropId pid) {
        return AttributeKey.valueOf(pid.toString());
    }

    final PropId id;
    final NetworkClient owner;
    final Attribute<T> attr;

    NettyMutProp(PropId id, NetworkClient owner, Attribute<T> attr) {
        this.id = id;
        this.owner = owner;
        this.attr = attr;
    }

    @Override
    public PropId getId() {
        return id;
    }

    @Override
    public NetworkClient getOwner() {
        return owner;
    }

    @Override
    public T get() {
        T val = attr.get();
        if (val == null) {
            throw new NullPointerException();
        }
        return val;
    }

    @Nullable
    @Override
    public T orNull() {
        return attr.get();
    }

    @Override
    public Optional<T> orEmpty() {
        return Optional.ofNullable(attr.get());
    }

    @Override
    public boolean isDefined() {
        return attr.get() != null;
    }

    @Override
    public void set(@Nullable T param) {
        if (param == null) {
            attr.remove();
        } else {
            attr.set(param);
        }
    }

    @Override
    public void forget() {
        attr.remove();
    }

    @Override
    public int hashCode() {
        Object val = attr.get();
        return val != null ? val.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || !(obj instanceof Prop)) return false;
        Prop that = (Prop) obj;

        Object a = that.orNull();
        Object b = this.orNull();

        return a == b || a != null && b != null && a.equals(b);
    }
}
