package org.rocket.network.guice;

import com.google.inject.Key;
import com.google.inject.util.Types;
import org.rocket.network.MutProp;
import org.rocket.network.Prop;

import java.lang.annotation.Annotation;
import java.util.Set;

public final class RocketGuiceUtil {
    private RocketGuiceUtil() {}

    @SuppressWarnings("unchecked")
    public static Key<Prop<?>> wrapProp(Key<?> key) {
        return (Key<Prop<?>>) key.ofType(Types.newParameterizedType(Prop.class, key.getTypeLiteral().getType()));
    }

    @SuppressWarnings("unchecked")
    public static Key<MutProp<?>> wrapMutProp(Key<?> key) {
        return (Key<MutProp<?>>) key.ofType(Types.newParameterizedType(MutProp.class, key.getTypeLiteral().getType()));
    }

    @SuppressWarnings("unchecked")
    public static Key<Set<Object>> controllersKeyFor(Class<? extends Annotation> controllerAnnotation) {
        return (Key<Set<Object>>) Key.get(Types.setOf(Object.class), controllerAnnotation);
    }
}
