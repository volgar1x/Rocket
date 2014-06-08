package org.rocket.network.event.acara;

import com.github.blackrush.acara.ListenerMetadata;
import org.fungsi.Either;
import org.junit.Before;
import org.junit.Test;
import org.rocket.network.NetworkClient;
import org.rocket.network.event.RecoverEvent;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RocketDispatcherTest {

    private ListenerMetadata metadata;
    private RocketDispatcher dispatcher;
    private NetworkClient client;

    @Before
    public void setUp() throws Exception {
        metadata = new ListenerMetadata(
                SomeListener.class,
                SomeListener.class.getDeclaredMethod("recover", NetworkClient.class, Throwable.class),
                new RocketEventWithComponentMetadata<>(
                        RecoverEvent.class,
                        Throwable.class,
                        RecoverEvent::getError
                )
        );
        dispatcher = new RocketDispatcher(metadata);
        client = mock(NetworkClient.class);
    }

    @Test
    public void testDispatch() throws Exception {
        // given
        RecoverEvent recoverEvent = new RecoverEvent<>(client, new Error());
        SomeListener listener = new SomeListener();

        // when
        Either<Object, Throwable> res = dispatcher.dispatch(listener, recoverEvent);

        // then
        assertTrue("dispatcher has succeeded", res.isLeft());
    }
}