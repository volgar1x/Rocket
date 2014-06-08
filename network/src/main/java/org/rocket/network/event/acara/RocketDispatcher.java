package org.rocket.network.event.acara;

import com.github.blackrush.acara.ListenerMetadata;
import com.github.blackrush.acara.dispatch.Dispatcher;
import org.fungsi.Either;
import org.rocket.network.event.*;

import java.lang.reflect.InvocationTargetException;

public class RocketDispatcher implements Dispatcher {
    private final ListenerMetadata metadata;

    public RocketDispatcher(ListenerMetadata metadata) {
        this.metadata = metadata;
    }

    public static Object[] unfold(Object event) {
        if (event instanceof ConnectEvent) {
            return new Object[] {((ConnectEvent) event).getClient()};
        } else if (event instanceof DisconnectEvent) {
            return new Object[] {((DisconnectEvent) event).getClient()};
        } else if (event instanceof ReceiveEvent) {
            ReceiveEvent evt = (ReceiveEvent) event;
            return new Object[] {evt.getClient(), evt.getMessage()};
        } else if (event instanceof RecoverEvent) {
            RecoverEvent evt = (RecoverEvent) event;
            return new Object[] {evt.getClient(), evt.getError()};
        }

        throw new Error();
    }

    @Override
    public Either<Object, Throwable> dispatch(Object listener, Object event) {
        Object[] unfolded = unfold(event);
        try {
            return Either.success(metadata.getListenerMethod().invoke(listener, unfolded));
        } catch (IllegalAccessException e) {
            return Either.failure(e);
        } catch (InvocationTargetException e) {
            return Either.failure(e.getTargetException());
        }
    }
}
