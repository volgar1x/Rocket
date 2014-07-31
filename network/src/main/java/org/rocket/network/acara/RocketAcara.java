package org.rocket.network.acara;

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
                .addMetadataLookup(RocketAcara::lookupBasic)

                // @Receive
                .addEventMetadataLookup(RocketReceiveEventMetadata::lookup)
                .addDispatcherLookup(RocketReceiveDispatcher::lookup)
                .addMetadataLookup(RocketAcara::lookupReceive)
                ;
    }

    static Stream<ListenerMetadata> lookupBasic(Object listener) {
        Class<?> klass = listener.getClass();

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

    static Stream<ListenerMetadata> lookupReceive(Object listener) {
        Class<?> klass = listener.getClass();

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
