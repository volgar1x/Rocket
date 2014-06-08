package org.rocket.network.event.acara;

import com.github.blackrush.acara.ListenerMetadata;
import com.github.blackrush.acara.dispatch.Dispatcher;
import com.github.blackrush.acara.dispatch.DispatcherLookup;

import java.util.Optional;

public class RocketDispatcherLookup implements DispatcherLookup {
    @Override
    public Optional<Dispatcher> lookup(ListenerMetadata metadata) {
        if (RocketListenerMetadataLookup.isValidListener(metadata.getListenerMethod())) {
            return Optional.of(new RocketDispatcher(metadata));
        }
        return Optional.empty();
    }
}
