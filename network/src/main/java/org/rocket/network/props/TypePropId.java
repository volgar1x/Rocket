package org.rocket.network.props;

import org.rocket.network.PropId;

import java.lang.reflect.Type;

final class TypePropId implements PropId {
    final Type type;

    TypePropId(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypePropId that = (TypePropId) o;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return "TypePropId{" +
                "type=" + type +
                '}';
    }
}
