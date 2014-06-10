package org.rocket.network.event.acara;

import com.github.blackrush.acara.EventMetadata;

import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class RocketEventWithComponentMetadata<T> implements EventMetadata {
    private final Class<T> eventClass;
    private final Class<?> componentClass;
    private final Function<T, Object> getComponentFunction;

    public RocketEventWithComponentMetadata(Class<T> eventClass, Class<?> componentClass, Function<T, Object> getComponentFunction) {
        this.eventClass = requireNonNull(eventClass, "eventClass");
        this.componentClass = requireNonNull(componentClass, "componentClass");
        this.getComponentFunction = requireNonNull(getComponentFunction, "getComponentFunction");
    }

    @Override
    public Class<?> getRawEventClass() {
        return eventClass;
    }

    public Class<?> getComponentClass() {
        return componentClass;
    }

    @Override
    public Stream<EventMetadata> getParent() {
        Class<?> superclass = componentClass.getSuperclass();
        return superclass == Object.class
                ? Stream.empty()
                : Stream.of(new RocketEventWithComponentMetadata<T>(eventClass, superclass, getComponentFunction))
                ;
    }

    @Override
    public boolean accept(Object event) {
        if (!eventClass.isInstance(event)) {
            return false;
        }
        Object component = getComponentFunction.apply(eventClass.cast(event));
        return componentClass.isInstance(component);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RocketEventWithComponentMetadata that = (RocketEventWithComponentMetadata) o;

        return componentClass.equals(that.componentClass) &&
               eventClass.equals(that.eventClass);
    }

    @Override
    public int hashCode() {
        int result = eventClass.hashCode();
        result = 31 * result + componentClass.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RocketEventWithComponentMetadata(" +
                "eventClass=" + eventClass +
                ", componentClass=" + componentClass +
                ')';
    }
}
