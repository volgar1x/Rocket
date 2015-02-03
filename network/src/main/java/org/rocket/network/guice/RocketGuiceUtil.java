package org.rocket.network.guice;

import com.google.inject.Key;
import com.google.inject.util.Types;

import java.lang.annotation.Annotation;
import java.util.Set;

public final class RocketGuiceUtil {
    private RocketGuiceUtil() {}

    @SuppressWarnings("unchecked")
    public static Key<Set<Object>> controllersKeyFor(Class<? extends Annotation> controllerAnnotation) {
        return (Key<Set<Object>>) Key.get(Types.setOf(Object.class), controllerAnnotation);
    }
}
