package org.rocket.network.acara;

import com.github.blackrush.acara.*;
import com.google.common.collect.ImmutableList;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.fungsi.concurrent.Worker;
import org.rocket.network.*;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.NetworkEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.SuperviseEvent;
import org.rocket.network.props.PropValidations;
import org.rocket.network.props.PropValidatorInstantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

final class RocketListenerBuilder extends JavaListenerBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RocketListenerBuilder.class);
    public static final RocketListenerBuilder instance = new RocketListenerBuilder(PropValidations::reflectiveInstantiator);

    private final PropValidatorInstantiator validationInstantator;

    RocketListenerBuilder(PropValidatorInstantiator validationInstantator) {
        this.validationInstantator = validationInstantator;
    }

    @Override
    protected Stream<Listener> scan(Method method) {
        if (method.isAnnotationPresent(Connect.class) || method.isAnnotationPresent(Disconnect.class)) {
            if (method.getParameterCount() != 0) {
                Annotation ann = method.getAnnotation(Connect.class);
                if (ann == null) {
                    ann = method.getAnnotation(Disconnect.class);
                }
                logger.warn("{} is annotated with {} but has an invalid signature", method, ann);
                return Stream.empty();
            }

            boolean disconnecting = method.isAnnotationPresent(Disconnect.class);
            TypedEventMetadata<ConnectEvent> metadata = new Events.ConnectEventMetadata(disconnecting);
            ConnectEventListener connectlistener = new ConnectEventListener(metadata, method);

            // @Connect we don't want to crash a freshly created connection, soft validation then
            // @Disconnect the connection is gone, does it really matter if a hard validation fails? (hint: no)
            Listener listener = wrapValidationIfNeeded(connectlistener, false);

            return Stream.of(listener);
        } else if (method.isAnnotationPresent(Receive.class)) {
            Receive annotation = method.getAnnotation(Receive.class);
            if (method.getParameterCount() != 1) {
                logger.warn("{} is annotated with org.rocket.network.Receive but has an invalid signature", method);
                return Stream.empty();
            }

            Class<?> messageClass = method.getParameterTypes()[0];
            TypedEventMetadata<ReceiveEvent> metadata = new Events.ComponentWiseEventMetadata<>(messageClass);
            ReceiveEventListener receivelistener = new ReceiveEventListener(metadata, method);

            // @Receive may or may not want hard validation
            // it will hard validate by default, and that's the recommended setting
            // but sometimes one may want a soft validation instead
            Listener listener = wrapValidationIfNeeded(receivelistener, !annotation.softValidation());

            return Stream.of(listener);
        } else if (method.isAnnotationPresent(Supervise.class)) {
            if (method.getParameterCount() != 1) {
                logger.warn("{} is annotated with org.rocket.network.Supervise but has an invalid signature", method);
                return Stream.empty();
            }

            Class<?> exceptionClass = method.getParameterTypes()[0];
            TypedEventMetadata<SuperviseEvent> metadata = new Events.ComponentWiseEventMetadata<>(exceptionClass);
            SuperviseEventListener superviselistener = new SuperviseEventListener(metadata, method);

            // @Supervise is the last shot to make things work
            // we do not want hard validations that might get things even worse
            Listener listener = wrapValidationIfNeeded(superviselistener, false);

            return Stream.of(listener);
        }
        return Stream.empty();
    }

    Listener wrapValidationIfNeeded(JavaListener<?> listener, boolean hard) {
        List<PropValidator> validators = PropValidations.fetchValidators(listener.getBehavior(), validationInstantator);
        if (validators.isEmpty()) {
            return listener;
        }

        PropValidator validator = PropValidator.aggregate(ImmutableList.copyOf(validators));
        return hard ? new HardValidator(listener, validator)
                    : new SoftValidator(listener, validator);
    }

    static final class ConnectEventListener extends JavaListener<ConnectEvent> {
        ConnectEventListener(TypedEventMetadata<ConnectEvent> signature, Method behavior) {
            super(signature, behavior);
        }

        @Override
        protected Object invoke(Object state, Method behavior, ConnectEvent event) throws Throwable {
            return behavior.invoke(state);
        }
    }

    static final class ReceiveEventListener extends JavaListener<ReceiveEvent> {
        ReceiveEventListener(TypedEventMetadata<ReceiveEvent> signature, Method behavior) {
            super(signature, behavior);
        }

        @Override
        protected Object invoke(Object state, Method behavior, ReceiveEvent event) throws Throwable {
            return behavior.invoke(state, event.getMessage());
        }
    }

    static final class SuperviseEventListener extends JavaListener<SuperviseEvent> {
        SuperviseEventListener(TypedEventMetadata<SuperviseEvent> signature, Method behavior) {
            super(signature, behavior);
        }

        @Override
        protected Object invoke(Object state, Method behavior, SuperviseEvent event) throws Throwable {
            return behavior.invoke(state, event.getException());
        }
    }

    static final class HardValidator extends Listener {
        final Listener underlying;
        final PropValidator validator;

        HardValidator(Listener underlying, PropValidator validator) {
            this.underlying = underlying;
            this.validator = validator;
        }

        @Override
        public EventMetadata getHandledEvent() {
            return underlying.getHandledEvent();
        }

        @Override
        public Future<Object> dispatch(Object state, Object event, Worker worker) {
            NetworkEvent evt = (NetworkEvent) event;
            NetworkClient client = evt.getClient();
            return Future.constant(validator.validate(client))
                .flatMap(u -> underlying.dispatch(state, event, worker));
        }
    }

    static final class SoftValidator extends Listener {
        final Listener underlying;
        final PropValidator validator;

        SoftValidator(Listener underlying, PropValidator validator) {
            this.underlying = underlying;
            this.validator = validator;
        }

        @Override
        public EventMetadata getHandledEvent() {
            return underlying.getHandledEvent();
        }

        @Override
        public Future<Object> dispatch(Object state, Object event, Worker worker) {
            NetworkEvent evt = (NetworkEvent) event;
            NetworkClient client = evt.getClient();
            if (!validator.softValidate(client)) {
                return underlying.dispatch(state, event, worker);
            }
            //noinspection unchecked
            return (Future) Futures.unit();
        }
    }
}
