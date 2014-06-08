package org.rocket.network.event.acara;

import com.github.blackrush.acara.ListenerMetadata;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RocketListenerMetadataLookupTest {

    private RocketListenerMetadataLookup lookup;

    @Before
    public void setUp() throws Exception {
        lookup = new RocketListenerMetadataLookup();
    }

    @Test
    public void testLookup() throws Exception {
        // given
        SomeListener listener = new SomeListener();

        // when
        List<ListenerMetadata> res = lookup.lookup(listener).collect(Collectors.toList());

        // then
        assertThat("result size", res.size(), equalTo(1));

        ListenerMetadata metadata = res.get(0);
        assertThat("listener class", metadata.getListenerClass(), equalTo(SomeListener.class));
        assertThat("handled event class", metadata.getHandledEventClass(), equalTo(Throwable.class));
        assertThat("listener method name", metadata.getListenerMethod().getName(), equalTo("recover"));
    }
}