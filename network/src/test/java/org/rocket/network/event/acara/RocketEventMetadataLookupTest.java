package org.rocket.network.event.acara;

import com.github.blackrush.acara.EventMetadata;
import org.junit.Before;
import org.junit.Test;
import org.rocket.network.NetworkClient;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.DisconnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.RecoverEvent;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RocketEventMetadataLookupTest {

    private RocketEventMetadataLookup lookup;
    private NetworkClient client;

    @Before
    public void setUp() throws Exception {
        lookup = RocketEventMetadataLookup.SHARED;
        client = mock(NetworkClient.class);
    }

    @Test
    public void testConnect() throws Exception {
        // given
        ConnectEvent event = new ConnectEvent<>(client);

        // when
        Optional<EventMetadata> res = lookup.lookup(event);

        // then
        EventMetadata em = res.get();
        assertTrue("metadata is an instance of RocketEventMetadata", em instanceof RocketEventMetadata);
        assertThat("metadata raw event class", em.getRawEventClass(), equalTo(ConnectEvent.class));
    }

    @Test
    public void testDisconnect() throws Exception {
        // given
        DisconnectEvent event = new DisconnectEvent<>(client);

        // when
        Optional<EventMetadata> res = lookup.lookup(event);

        // then
        EventMetadata em = res.get();
        assertTrue("metadata is an instance of RocketEventMetadata", em instanceof RocketEventMetadata);
        assertThat("metadata raw event class", em.getRawEventClass(), equalTo(DisconnectEvent.class));
    }

    @Test
    public void testReceive() throws Exception {
        // given
        ReceiveEvent msg = new ReceiveEvent<>(client, "msg");

        // when
        Optional<EventMetadata> res = lookup.lookup(msg);

        // then
        RocketEventWithComponentMetadata em = (RocketEventWithComponentMetadata) res.get();
        assertThat("metadata raw event class", em.getRawEventClass(), equalTo(ReceiveEvent.class));
        assertThat("metadata component class", em.getComponentClass(), equalTo(String.class));
    }

    @Test
    public void testRecover() throws Exception {
        // given
        RecoverEvent msg = new RecoverEvent<>(client, new NullPointerException());

        // when
        Optional<EventMetadata> res = lookup.lookup(msg);

        // then
        RocketEventWithComponentMetadata em = (RocketEventWithComponentMetadata) res.get();
        assertThat("metadata raw event class", em.getRawEventClass(), equalTo(RecoverEvent.class));
        assertThat("metadata component class", em.getComponentClass(), equalTo(NullPointerException.class));
    }
}