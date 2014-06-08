package org.rocket.network.event.acara;

import com.github.blackrush.acara.EventMetadata;
import com.github.blackrush.acara.ListenerMetadata;
import com.github.blackrush.acara.ListenerMetadataLookup;
import org.rocket.network.event.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RocketListenerMetadataLookup implements ListenerMetadataLookup {
    public static Optional<Annotation> getAnnotation(Method method) {
        return new AnnotationVisitor<Annotation>() {
            @Override
            public Annotation visitConnect(Connect ann) {
                return ann;
            }

            @Override
            public Annotation visitDisconnect(Disconnect ann) {
                return ann;
            }

            @Override
            public Annotation visitReceive(Receive ann) {
                return ann;
            }

            @Override
            public Annotation visitRecover(Recover ann) {
                return ann;
            }
        }.visit(method);
    }

    public static boolean isValidListener(Method method) {
        return getAnnotation(method).filter(ann -> new AnnotationVisitor<Boolean>(){
            @Override
            public Boolean visitConnect(Connect ann) {
                return ann.enabled();
            }

            @Override
            public Boolean visitDisconnect(Disconnect ann) {
                return ann.enabled();
            }

            @Override
            public Boolean visitReceive(Receive ann) {
                return ann.enabled();
            }

            @Override
            public Boolean visitRecover(Recover ann) {
                return ann.enabled();
            }
        }.visit(ann).orElse(false)).isPresent();
    }

    public static Stream<Class<?>> traverseInheritance(Class<?> klass) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<Class<?>>(Long.MAX_VALUE, 0) {
            Class<?> cur = klass;

            @Override
            public boolean tryAdvance(Consumer<? super Class<?>> action) {
                action.accept(cur);
                if (cur.getSuperclass() == Object.class) return false;
                cur = cur.getSuperclass();
                return true;
            }
        }, false);
    }

    public static Stream<Method> getDeclaredMethods(Class<?> klass) {
        return Stream.of(klass.getDeclaredMethods());
    }

    public static EventMetadata getHandledEventMetadata(Method method) {
        return new AnnotationVisitor<EventMetadata>() {
            @Override
            public EventMetadata visitConnect(Connect ann) {
                return new RocketEventMetadata(ConnectEvent.class);
            }

            @Override
            public EventMetadata visitDisconnect(Disconnect ann) {
                return new RocketEventMetadata(DisconnectEvent.class);
            }

            @Override
            public EventMetadata visitReceive(Receive ann) {
                return new RocketEventWithComponentMetadata<>(
                        ReceiveEvent.class,
                        method.getParameterTypes()[1],
                        ReceiveEvent::getMessage
                );
            }

            @Override
            public EventMetadata visitRecover(Recover ann) {
                return new RocketEventWithComponentMetadata<>(
                        RecoverEvent.class,
                        method.getParameterTypes()[1],
                        RecoverEvent::getError
                );
            }
        }.visit(method).get();
    }

    @Override
    public Stream<ListenerMetadata> lookup(Object listener) {
        return traverseInheritance(listener.getClass())
                .flatMap(RocketListenerMetadataLookup::getDeclaredMethods)
                .filter(RocketListenerMetadataLookup::isValidListener)
                .map(method -> new ListenerMetadata(listener.getClass(), method, getHandledEventMetadata(method)));
    }
}
