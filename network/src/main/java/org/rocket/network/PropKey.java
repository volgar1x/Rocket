package org.rocket.network;

import com.google.common.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class PropKey {
    private final TypeToken<?> component;
    private final Optional<Class<? extends Annotation>> annotation;

    private PropKey(TypeToken<?> component, Optional<Class<? extends Annotation>> annotation) {
        this.component = requireNonNull(component, "component");
        this.annotation = requireNonNull(annotation, "annotation");
    }

    public static PropKey of(TypeToken<?> component, Optional<Class<? extends Annotation>> annotation) {
        return new PropKey(component, annotation);
    }

    public static PropKey of(TypeToken<?> component) {
        return of(component, Optional.empty());
    }

    public static PropKey of(TypeToken<?> component, Class<? extends Annotation> annotation) {
        return of(component, Optional.of(annotation));
    }

    public TypeToken<?> getComponent() {
        return component;
    }

    public Optional<Class<? extends Annotation>> getAnnotation() {
        return annotation;
    }

    public PropKey withComponent(TypeToken<?> component) {
        return new PropKey(component, annotation);
    }

    public PropKey withAnnotation(Optional<Class<? extends Annotation>> annotation) {
        return new PropKey(component, annotation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropKey propKey = (PropKey) o;

        return annotation.equals(propKey.annotation) &&
                component.equals(propKey.component);

    }

    @Override
    public int hashCode() {
        int result = component.hashCode();
        result = 31 * result + annotation.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PropKey(" +
                "component=" + component +
                ", annotation=" + annotation +
                ')';
    }
}
