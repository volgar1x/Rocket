package org.rocket.network.event.acara;

import org.junit.Before;
import org.junit.Test;
import org.rocket.network.NetworkClient;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.DisconnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.RecoverEvent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
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
        RocketEventMetadata res = (RocketEventMetadata) lookup.lookup(event).get();

        // then
        assertThat("metadata raw event class", res.getRawEventClass(), equalTo(ConnectEvent.class));
        assertThat("metadata parent count", res.getParent().count(), equalTo(0L));
    }

    @Test
    public void testDisconnect() throws Exception {
        // given
        DisconnectEvent event = new DisconnectEvent<>(client);

        // when
        RocketEventMetadata res = (RocketEventMetadata) lookup.lookup(event).get();

        // then
        assertThat("metadata raw event class", res.getRawEventClass(), equalTo(DisconnectEvent.class));
        assertThat("metadata parent count", res.getParent().count(), equalTo(0L));
    }

    @Test
    public void testReceive() throws Exception {
        // given
        ReceiveEvent msg = new ReceiveEvent<>(client, "msg");

        // when
        RocketEventWithComponentMetadata res = (RocketEventWithComponentMetadata) lookup.lookup(msg).get();

        // then
        assertThat("metadata raw event class", res.getRawEventClass(), equalTo(ReceiveEvent.class));
        assertThat("metadata component class", res.getComponentClass(), equalTo(String.class));
        assertThat("metadata parent count", res.getParent().count(), equalTo(0L));
    }

    @Test
    public void testRecover() throws Exception {
        // given
        RecoverEvent msg = new RecoverEvent<>(client, new NullPointerException());

        // when
        RocketEventWithComponentMetadata res = (RocketEventWithComponentMetadata) lookup.lookup(msg).get();

        // then
        assertThat("metadata raw event class", res.getRawEventClass(), equalTo(RecoverEvent.class));
        assertThat("metadata component class", res.getComponentClass(), equalTo(NullPointerException.class));
        assertThat("metadata parent count", res.getParent().count(), equalTo(1L));
    }
}