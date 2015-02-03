package org.rocket.network.acara;

import com.github.blackrush.acara.*;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Worker;
import org.rocket.network.Connect;
import org.rocket.network.Disconnect;
import org.rocket.network.Receive;
import org.rocket.network.Supervise;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.SuperviseEvent;

import java.lang.reflect.Method;
import java.util.stream.Stream;

final class RocketListenerBuilder extends JavaListenerBuilder {
    public static final RocketListenerBuilder instance = new RocketListenerBuilder();

    @Override
    protected Stream<Listener> scan(Object o, Method method) {
        if (method.isAnnotationPresent(Connect.class) || method.isAnnotationPresent(Disconnect.class)) {
            boolean disconnecting = method.isAnnotationPresent(Disconnect.class);
            EventMetadata metadata = new Events.ConnectEventMetadata(disconnecting);
            Listener listener = new ConnectEventListener(metadata, o, method);
            return Stream.of(listener);
        } else if (method.isAnnotationPresent(Receive.class)) {
            if (method.getParameterCount() != 1) {
                // TODO warn user
                return Stream.empty();
            }

            Class<?> messageClass = method.getParameterTypes()[0];
            TypedEventMetadata<ReceiveEvent> metadata = new Events.ComponentWiseEventMetadata<>(messageClass);
            Listener listener = new ReceiveEventListener(metadata, o, method);
            return Stream.of(listener);
        } else if (method.isAnnotationPresent(Supervise.class)) {
            if (method.getParameterCount() != 1) {
                // TODO warn user
                return Stream.empty();
            }

            Class<?> exceptionClass = method.getParameterTypes()[0];
            TypedEventMetadata<SuperviseEvent> metadata = new Events.ComponentWiseEventMetadata<>(exceptionClass);
            Listener listener = new SuperviseEventListener(metadata, o, method);
            return Stream.of(listener);
        }
        return Stream.empty();
    }

    final class ConnectEventListener extends Listener {
        final EventMetadata metadata;
        final Object state;
        final Method behavior;

        ConnectEventListener(EventMetadata metadata, Object state, Method behavior) {
            this.metadata = metadata;
            this.state = state;
            this.behavior = behavior;
        }

        @Override
        public EventMetadata getHandledEvent() {
            return metadata;
        }

        @Override
        public Future<Object> dispatch(Object event, Worker worker) {
            return worker.submit(() -> behavior.invoke(state));
        }
    }

    final class ReceiveEventListener extends JavaListener<ReceiveEvent> {
        ReceiveEventListener(TypedEventMetadata<ReceiveEvent> signature, Object state, Method behavior) {
            super(signature, state, behavior);
        }

        @Override
        protected Object invoke(Object state, Method behavior, ReceiveEvent event) throws Throwable {
            return behavior.invoke(state, event.getMessage());
        }
    }

    final class SuperviseEventListener extends JavaListener<SuperviseEvent> {
        SuperviseEventListener(TypedEventMetadata<SuperviseEvent> signature, Object state, Method behavior) {
            super(signature, state, behavior);
        }

        @Override
        protected Object invoke(Object state, Method behavior, SuperviseEvent event) throws Throwable {
            return behavior.invoke(state, event.getException());
        }
    }
}
