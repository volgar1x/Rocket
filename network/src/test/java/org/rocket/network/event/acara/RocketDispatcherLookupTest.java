package org.rocket.network.event.acara;

import com.github.blackrush.acara.ListenerMetadata;
import com.github.blackrush.acara.dispatch.Dispatcher;
import org.junit.Before;
import org.junit.Test;
import org.rocket.network.NetworkClient;
import org.rocket.network.event.RecoverEvent;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class RocketDispatcherLookupTest {

    private RocketDispatcherLookup lookup;

    @Before
    public void setUp() throws Exception {
        lookup = new RocketDispatcherLookup();

    }

    @Test
    public void testLookup() throws Exception {
        // given
        ListenerMetadata metadata = new ListenerMetadata(
                SomeListener.class,
                SomeListener.class.getDeclaredMethod("recover", NetworkClient.class, Throwable.class),
                new RocketEventWithComponentMetadata<>(
                        RecoverEvent.class,
                        Throwable.class,
                        RecoverEvent::getError
                )
        );

        // when
        Optional<Dispatcher> opt = lookup.lookup(metadata);

        // then
        assertTrue("opt.isPresent()", opt.isPresent());
        assertTrue("opt.get() instanceof RocketDispatcher", opt.get() instanceof RocketDispatcher);
    }
}