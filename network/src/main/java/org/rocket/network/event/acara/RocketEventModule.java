package org.rocket.network.event.acara;

import com.github.blackrush.acara.EventBusBuilder;
import com.github.blackrush.acara.EventModule;

public final class RocketEventModule implements EventModule {
    @Override
    public EventBusBuilder configure(EventBusBuilder builder) {
        return builder
                .addMetadataLookup(new RocketListenerMetadataLookup())
                .addDispatcherLookup(new RocketDispatcherLookup())
                .addEventMetadataLookup(new RocketEventMetadataLookup())
                ;
    }
}
