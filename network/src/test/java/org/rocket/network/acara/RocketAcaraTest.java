package org.rocket.network.acara;

import com.github.blackrush.acara.EventMetadata;
import com.github.blackrush.acara.Listener;
import com.github.blackrush.acara.ListenerMetadata;
import com.github.blackrush.acara.dispatch.Dispatcher;
import org.junit.Test;
import org.rocket.network.PropValidation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class RocketAcaraTest {

    @PropValidation(value = Byte.class, present = true)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnotherAnnotation {}

    @PropValidation(value = String.class, present = true)
    @PropValidation(value = Integer.class, present = true)
    @AnotherAnnotation
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SomeAnnotation {}

    @SomeAnnotation
    public static class Hook {}

    @Test
    public void testLookupPropAnnotations() throws Exception {
        List<Validations.Validation> validations = RocketAcara.lookupPropValidations(Hook.class);

        assertEquals("number of validations", 3, validations.size());
    }

    @Test
    public void testWrapInPropValidatorIfNeeded() throws Exception {
        // given
        @SomeAnnotation
        class TheListener {
            @Listener
            public void event(Object evt) {

            }

            @AnotherAnnotation
            @Listener
            public void event2(Object evt) {

            }
        }

        EventMetadata eventMetadata1 = mock(EventMetadata.class);
        EventMetadata eventMetadata2 = mock(EventMetadata.class);

        Dispatcher dispatcher1 = mock(Dispatcher.class);
        Dispatcher dispatcher2 = mock(Dispatcher.class);

        ListenerMetadata listener1 = new ListenerMetadata(
                TheListener.class,
                TheListener.class.getMethod("event", Object.class),
                eventMetadata1
        );

        ListenerMetadata listener2 = new ListenerMetadata(
                TheListener.class,
                TheListener.class.getMethod("event2", Object.class),
                eventMetadata2
        );

        // when
        RocketPropValidatorDispatcher newDispatcher1 = (RocketPropValidatorDispatcher)
                RocketAcara.wrapInPropValidatorIfNeeded(dispatcher1, listener1);
        RocketPropValidatorDispatcher newDispatcher2 = (RocketPropValidatorDispatcher)
                RocketAcara.wrapInPropValidatorIfNeeded(dispatcher2, listener2);

        // then
        assertEquals(dispatcher1, newDispatcher1.dispatcher);
        assertEquals(3, newDispatcher1.validations.size());
        assertEquals(dispatcher2, newDispatcher2.dispatcher);
        assertEquals(1, newDispatcher2.validations.size());
    }
}