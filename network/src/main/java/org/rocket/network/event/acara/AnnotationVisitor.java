package org.rocket.network.event.acara;

import org.rocket.network.event.Connect;
import org.rocket.network.event.Disconnect;
import org.rocket.network.event.Receive;
import org.rocket.network.event.Recover;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

public interface AnnotationVisitor<T> {
    T visitConnect(Connect ann);
    T visitDisconnect(Disconnect ann);
    T visitReceive(Receive ann);
    T visitRecover(Recover ann);
    
    default Optional<T> visit(Annotation ann) {
        if (ann instanceof Connect) {
            return Optional.of(visitConnect((Connect) ann));
        } else if (ann instanceof Disconnect) {
            return Optional.of(visitDisconnect((Disconnect) ann));
        } else if (ann instanceof Receive) {
            return Optional.of(visitReceive((Receive) ann));
        } else if (ann instanceof Recover) {
            return Optional.of(visitRecover((Recover) ann));
        }
        return Optional.empty();
    }
    
    default Optional<T> visit(AnnotatedElement element) {
        Connect connect = element.getAnnotation(Connect.class);
        if (connect != null) return Optional.of(visitConnect(connect));
        Disconnect disconnect = element.getAnnotation(Disconnect.class);
        if (disconnect != null) return Optional.of(visitDisconnect(disconnect));
        Receive receive = element.getAnnotation(Receive.class);
        if (receive != null) return Optional.of(visitReceive(receive));
        Recover recover = element.getAnnotation(Recover.class);
        if (recover != null) return Optional.of(visitRecover(recover));
        return Optional.empty();
    }
}
