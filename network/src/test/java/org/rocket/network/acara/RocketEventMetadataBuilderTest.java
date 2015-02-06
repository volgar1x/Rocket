package org.rocket.network.acara;

import com.github.blackrush.acara.EventMetadata;
import org.junit.Test;
import org.rocket.network.NetworkClient;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.SuperviseEvent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;

public class RocketEventMetadataBuilderTest {

    @Test
    public void testBuild() throws Exception {
        RocketEventMetadataBuilder b = RocketEventMetadataBuilder.instance;
        NetworkClient client = mock(NetworkClient.class);

        EventMetadata connect = b.build(new ConnectEvent(client, false));
        EventMetadata disconnect = b.build(new ConnectEvent(client, true));
        EventMetadata receive = b.build(new ReceiveEvent(client, "foobar"));
        EventMetadata supervise = b.build(new SuperviseEvent(client, new Exception()));
        EventMetadata foobar = b.build("foobar");

        assertThat("connect", connect, notNullValue());
        assertThat("disconnect", disconnect, notNullValue());
        assertThat("receive", receive, notNullValue());
        assertThat("supervise", supervise, notNullValue());
        assertThat("foobar", foobar, nullValue());
    }
}