package org.rocket.network.acara;

import com.github.blackrush.acara.dispatch.Dispatcher;
import com.google.inject.Key;
import org.fungsi.Either;
import org.rocket.network.NetworkClient;
import org.rocket.network.event.NetworkEvent;

import java.util.List;

final class RocketPropValidatorDispatcher implements Dispatcher {
    private final Dispatcher dispatcher;
    private final List<Key<?>> keys;

    public RocketPropValidatorDispatcher(Dispatcher dispatcher, List<Key<?>> keys) {
        this.dispatcher = dispatcher;
        this.keys = keys;
    }

    @Override
    public Either<Object, Throwable> dispatch(Object listener, Object o) {
        NetworkEvent event = (NetworkEvent) o;
        NetworkClient client = event.getClient();
        for (Key<?> key : keys) {
            if (!client.isPropPresent(key)) {
                throw new IllegalStateException("property " + key + " is not present");
            }
        }

        return dispatcher.dispatch(listener, event);
    }
}
