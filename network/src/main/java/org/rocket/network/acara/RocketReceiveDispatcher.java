package org.rocket.network.acara;

import com.github.blackrush.acara.ListenerMetadata;
import com.github.blackrush.acara.dispatch.Dispatcher;
import org.fungsi.Either;
import org.rocket.network.Receive;
import org.rocket.network.event.ReceiveEvent;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.fungsi.Unit.unit;

final class RocketReceiveDispatcher implements Dispatcher {
    private final Method method;
    private final Class<?> messageClass;
    private final boolean acceptsChildren;

    public RocketReceiveDispatcher(Method method, Class<?> messageClass, boolean acceptsChildren) {
        this.method = method;
        this.messageClass = messageClass;
        this.acceptsChildren = acceptsChildren;
    }

    @Override
    public Either<Object, Throwable> dispatch(Object listener, Object event) {
        Object message = ((ReceiveEvent) event).getMessage();

        if (!acceptsChildren && !messageClass.equals(message.getClass())) {
//            return Either.failure(new IllegalArgumentException(
//                    method + " does not accept children messages"));
            return Either.success(unit());
        }

        try {
            return Either.success(method.invoke(listener, message));
        } catch (Throwable t) {
            return Either.failure(t);
        }
    }

    public static Optional<Dispatcher> lookup(ListenerMetadata meta) {
        Method method = meta.getListenerMethod();
        Receive ann = method.getAnnotation(Receive.class);

        if (ann != null) {
            return Optional.of(new RocketReceiveDispatcher(
                    method,
                    method.getParameterTypes()[0],
                    ann.acceptsChildren()
            ));
        }

        return Optional.empty();
    }
}
