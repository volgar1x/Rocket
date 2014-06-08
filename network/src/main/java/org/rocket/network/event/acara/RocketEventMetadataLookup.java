package org.rocket.network.event.acara;

import com.github.blackrush.acara.EventMetadata;
import com.github.blackrush.acara.EventMetadataLookup;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.DisconnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.RecoverEvent;

import java.util.Optional;

public final class RocketEventMetadataLookup implements EventMetadataLookup {
    public static final RocketEventMetadataLookup SHARED = new RocketEventMetadataLookup();

    @Override
    public Optional<EventMetadata> lookup(Object event) {
        if (event instanceof ConnectEvent || event instanceof DisconnectEvent) {
            return Optional.of(new RocketEventMetadata(event.getClass()));
        } else if (event instanceof ReceiveEvent) {
            return Optional.of(new RocketEventWithComponentMetadata<>(
                    ReceiveEvent.class,
                    ((ReceiveEvent) event).getMessage().getClass(),
                    ReceiveEvent::getMessage
            ));
        } else if (event instanceof RecoverEvent) {
            return Optional.of(new RocketEventWithComponentMetadata<>(
                    RecoverEvent.class,
                    ((RecoverEvent) event).getError().getClass(),
                    RecoverEvent::getError
            ));
        }
        return Optional.empty();
    }
}
