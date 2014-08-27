package org.rocket.network.acara;

import com.github.blackrush.acara.EventMetadata;
import org.rocket.network.event.ConnectEvent;

import java.util.Optional;
import java.util.stream.Stream;

final class RocketBasicEventMetadata implements EventMetadata {

    private boolean disconnecting;

    public RocketBasicEventMetadata(boolean disconnecting) {
        this.disconnecting = disconnecting;
    }

    @Override
    public Class<?> getRawEventClass() {
        return ConnectEvent.class;
    }

    @Override
    public Stream<EventMetadata> getParent() {
        return Stream.empty();
    }

    @Override
    public boolean accept(Object event) {
        return event instanceof ConnectEvent &&
               ((ConnectEvent) event).isDisconnecting() == disconnecting;
    }

    private static final EventMetadata
            CONNECTING = new RocketBasicEventMetadata(false),
            DISCONNECTING = new RocketBasicEventMetadata(true)
            ;

    public static Optional<EventMetadata> lookup(Object event) {
        if (event instanceof ConnectEvent) {
            return Optional.of(((ConnectEvent) event).isDisconnecting()
                ? DISCONNECTING
                : CONNECTING);
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RocketBasicEventMetadata that = (RocketBasicEventMetadata) o;
        return disconnecting == that.disconnecting;

    }

    @Override
    public int hashCode() {
        return (disconnecting ? 1 : 0);
    }
}
