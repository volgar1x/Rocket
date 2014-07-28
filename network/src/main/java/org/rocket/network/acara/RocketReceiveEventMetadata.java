package org.rocket.network.acara;

import com.github.blackrush.acara.EventMetadata;
import org.rocket.network.event.ReceiveEvent;

import java.util.Optional;
import java.util.stream.Stream;

final class RocketReceiveEventMetadata implements EventMetadata {
    private final Class<?> messageClass;

    public RocketReceiveEventMetadata(Class<?> messageClass) {
        this.messageClass = messageClass;
    }

    @Override
    public Class<?> getRawEventClass() {
        return ReceiveEvent.class;
    }

    @Override
    public Stream<EventMetadata> getParent() {
        if (messageClass == Object.class) {
            return Stream.empty();
        }
        return Stream.of(new RocketReceiveEventMetadata(messageClass.getSuperclass()));
    }

    @Override
    public boolean accept(Object event) {
        return event instanceof ReceiveEvent &&
               messageClass.isInstance(((ReceiveEvent) event).getMessage());
    }

    public static Optional<EventMetadata> lookup(Object event) {
        if (event instanceof ReceiveEvent) {
            Object message = ((ReceiveEvent) event).getMessage();
            return Optional.of(new RocketReceiveEventMetadata(message.getClass()));
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RocketReceiveEventMetadata that = (RocketReceiveEventMetadata) o;
        return messageClass.equals(that.messageClass);

    }

    @Override
    public int hashCode() {
        return messageClass.hashCode();
    }
}
