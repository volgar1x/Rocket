package org.rocket.network.acara;

import com.github.blackrush.acara.ClassListenerMetadataLookup;
import com.github.blackrush.acara.EventModule;
import com.github.blackrush.acara.ListenerMetadata;
import org.rocket.network.Connect;
import org.rocket.network.Disconnect;
import org.rocket.network.Receive;

import java.util.stream.Stream;

public final class RocketAcara {
    private RocketAcara() {}

    public static EventModule newContextfulModule() {
        return builder -> builder
                // @Connect @Disconnect
                .addEventMetadataLookup(RocketBasicEventMetadata::lookup)
                .addDispatcherLookup(RocketBasicDispatcher::lookup)
                .addMetadataLookup(new BasicLookup())

                // @Receive
                .addEventMetadataLookup(RocketReceiveEventMetadata::lookup)
                .addDispatcherLookup(RocketReceiveDispatcher::lookup)
                .addMetadataLookup(new ReceiveLookup())
                ;
    }

    private static class BasicLookup extends ClassListenerMetadataLookup {
        @Override
        protected Stream<ListenerMetadata> lookupClass(Class<?> klass) {
            return Stream.concat(Stream.of(klass.getDeclaredMethods()), Stream.of(klass.getMethods()))
                    .distinct()
                    .flatMap(method -> {
                        boolean disconnecting = false;

                        if (method.isAnnotationPresent(Connect.class) || (disconnecting = method.isAnnotationPresent(Disconnect.class))) {
                            return Stream.of(new ListenerMetadata(
                                    klass,
                                    method,
                                    new RocketBasicEventMetadata(disconnecting)
                            ));
                        }

                        return Stream.empty();
                    });
        }
    }

    private static class ReceiveLookup extends ClassListenerMetadataLookup {
        @Override
        protected Stream<ListenerMetadata> lookupClass(Class<?> klass) {
            return Stream.concat(Stream.of(klass.getDeclaredMethods()), Stream.of(klass.getMethods()))
                    .distinct()
                    .flatMap(method -> {
                        if (method.isAnnotationPresent(Receive.class)) {
                            return Stream.of(new ListenerMetadata(
                                    klass,
                                    method,
                                    new RocketReceiveEventMetadata(method.getParameterTypes()[0])
                            ));
                        }

                        return Stream.empty();
                    });
        }
    }
}
