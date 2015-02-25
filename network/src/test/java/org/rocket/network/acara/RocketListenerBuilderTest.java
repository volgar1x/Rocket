package org.rocket.network.acara;

import com.github.blackrush.acara.Listener;
import com.google.common.util.concurrent.MoreExecutors;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Worker;
import org.fungsi.concurrent.Workers;
import org.junit.Before;
import org.junit.Test;
import org.rocket.MoreCollectors;
import org.rocket.network.*;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.SuperviseEvent;
import org.rocket.network.props.PropIds;
import org.rocket.network.props.PropPresence;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RocketListenerBuilderTest {

    private RocketListenerBuilder b;
    private NetworkClient client;
    private Worker worker;

    @Before
    public void setUp() throws Exception {
        b = new RocketListenerBuilder();
        client = mock(NetworkClient.class);
        worker = Workers.wrap(MoreExecutors.directExecutor());
    }

    static class WithConnect {
        @Connect public void method(){}
    }
    static class WithDisconnect {
        @Disconnect public void method() {}
    }
    static class WithReceive {
        @Receive public void method(String foobar) {}
    }
    static class WithSupervise {
        @Supervise public void method(NullPointerException e) {}
    }
    static class WithValidation {
        @PropPresence(String.class)
        @Receive
        public void hard(Object msg) {}

        @PropPresence(String.class)
        @Receive(softValidation = true)
        public void soft(Object msg) {}
    }
    static class Invalids {
        @Connect public void toomuch1(String arguments) {}
        @Disconnect public void toomuch2(String arguments) {}

        @Receive public void toofew1() {}
        @Supervise public void toofew2() {}
    }

    @Test
    public void testScanConnect() throws Exception {
        Listener l = b.build(new WithConnect()).collect(MoreCollectors.uniq());
        Events.ConnectEventMetadata meta = (Events.ConnectEventMetadata) l.getHandledEvent();

        Future<Object> resp = l.dispatch(new ConnectEvent(client, false), worker);

        assertFalse("disconnecting", meta.disconnecting);
        assertTrue("dispatch response is success", resp.isSuccess());
    }

    @Test
    public void testScanDisconnect() throws Exception {
        Listener l = b.build(new WithDisconnect()).collect(MoreCollectors.uniq());
        Events.ConnectEventMetadata meta = (Events.ConnectEventMetadata) l.getHandledEvent();

        Future<Object> resp = l.dispatch(new ConnectEvent(client, true), worker);

        assertTrue("disconnecting", meta.disconnecting);
        assertTrue("dispatch response is success", resp.isSuccess());
    }

    @Test
    public void testScanReceive() throws Exception {
        Listener l = b.build(new WithReceive()).collect(MoreCollectors.uniq());
        Events.ComponentWiseEventMetadata meta = (Events.ComponentWiseEventMetadata) l.getHandledEvent();

        Future<Object> resp = l.dispatch(new ReceiveEvent(client, "foobar"), worker);

        assertThat("component class", meta.componentClass, equalTo(String.class));
        assertTrue("dispatch response is success", resp.isSuccess());
    }

    @Test
    public void testScanSupervise() throws Exception {
        Listener l = b.build(new WithSupervise()).collect(MoreCollectors.uniq());
        Events.ComponentWiseEventMetadata meta = (Events.ComponentWiseEventMetadata) l.getHandledEvent();

        Future<Object> resp = l.dispatch(new SuperviseEvent(client, new NullPointerException()), worker);

        assertThat("component class", meta.componentClass, equalTo(NullPointerException.class));
        assertTrue("dispatch response is success", resp.isSuccess());
    }

    @Test
    public void testScanError() throws Exception {
        long count = b.build(new Invalids()).count();

        assertThat("scanned listeners", count, equalTo(0L));
    }

    @Test
    public void testScanValidated() throws Exception {
        @SuppressWarnings("unchecked")
        MutProp<Object> prop = mock(MutProp.class);
        PropId pid = PropIds.type(String.class);
        when(client.getProp(pid)).thenReturn(prop);

        Object[] tmp = b.build(new WithValidation()).toArray();
        Listener hard = (Listener) tmp[0];
        Listener soft = (Listener) tmp[1];

        Future<Object> hardResp = hard.dispatch(new ReceiveEvent(client, "foo"), worker);
        Future<Object> softResp = soft.dispatch(new ReceiveEvent(client, "bar"), worker);

        assertTrue("hard response is failure", hardResp.isFailure());
        assertTrue("soft response is success", softResp.isSuccess());
    }
}