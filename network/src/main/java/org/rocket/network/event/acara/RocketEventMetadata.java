package org.rocket.network.event.acara;

import com.github.blackrush.acara.EventMetadata;

import static java.util.Objects.requireNonNull;

public final class RocketEventMetadata implements EventMetadata {
    private final Class<?> eventClass;

    public RocketEventMetadata(Class<?> eventClass) {
        this.eventClass = requireNonNull(eventClass, "eventClass");
    }

    @Override
    public Class<?> getRawEventClass() {
        return eventClass;
    }

    @Override
    public boolean accept(Object event) {
        return eventClass.isInstance(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RocketEventMetadata that = (RocketEventMetadata) o;

        return eventClass.equals(that.eventClass);

    }

    @Override
    public int hashCode() {
        return eventClass.hashCode();
    }

    @Override
    public String toString() {
        return "RocketEventMetadata(" +
                "eventClass=" + eventClass +
                ')';
    }
}
