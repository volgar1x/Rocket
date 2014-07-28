package org.rocket.network.acara;

import com.github.blackrush.acara.ListenerMetadata;
import com.github.blackrush.acara.dispatch.Dispatcher;
import org.fungsi.Either;
import org.rocket.network.Connect;
import org.rocket.network.Disconnect;

import java.lang.reflect.Method;
import java.util.Optional;

final class RocketBasicDispatcher implements Dispatcher {
    private final Method method;

    public RocketBasicDispatcher(Method method) {
        this.method = method;
    }

    @Override
    public Either<Object, Throwable> dispatch(Object listener, Object event) {
        try {
            return Either.success(method.invoke(listener));
        } catch (Throwable t) {
            return Either.failure(t);
        }
    }

    public static Optional<Dispatcher> lookup(ListenerMetadata meta) {
        Method method = meta.getListenerMethod();

        if (method.isAnnotationPresent(Connect.class) || method.isAnnotationPresent(Disconnect.class)) {
            return Optional.of(new RocketBasicDispatcher(method));
        }

        return Optional.empty();
    }
}
