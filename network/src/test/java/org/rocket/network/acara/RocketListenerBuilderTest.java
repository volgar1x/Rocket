package org.rocket.network.acara;

import com.github.blackrush.acara.Listener;
import com.google.common.util.concurrent.MoreExecutors;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Worker;
import org.fungsi.concurrent.Workers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.rocket.MoreCollectors;
import org.rocket.network.*;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.SuperviseEvent;
import org.rocket.network.props.PropIds;
import org.rocket.network.props.PropPresence;
import org.rocket.network.props.PropValidations;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RocketListenerBuilderTest {

    private RocketListenerBuilder b;
    private NetworkClient client;
    private Worker worker;

    @Before
    public void setUp() throws Exception {
        b = new RocketListenerBuilder(PropValidations::reflectiveInstantiator);
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
        WithConnect state = new WithConnect();

        Listener l = b.build(state).collect(MoreCollectors.uniq());
        Events.ConnectEventMetadata meta = (Events.ConnectEventMetadata) l.getHandledEvent();

        Future<Object> resp = l.dispatch(state, new ConnectEvent(client, false), worker);

        assertFalse("disconnecting", meta.disconnecting);
        assertTrue("dispatch response is success", resp.isSuccess());
    }

    @Test
    public void testScanDisconnect() throws Exception {
        WithDisconnect state = new WithDisconnect();

        Listener l = b.build(state).collect(MoreCollectors.uniq());
        Events.ConnectEventMetadata meta = (Events.ConnectEventMetadata) l.getHandledEvent();

        Future<Object> resp = l.dispatch(state, new ConnectEvent(client, true), worker);

        assertTrue("disconnecting", meta.disconnecting);
        assertTrue("dispatch response is success", resp.isSuccess());
    }

    @Test
    public void testScanReceive() throws Exception {
        WithReceive state = new WithReceive();

        Listener l = b.build(state).collect(MoreCollectors.uniq());
        Events.ComponentWiseEventMetadata meta = (Events.ComponentWiseEventMetadata) l.getHandledEvent();

        Future<Object> resp = l.dispatch(state, new ReceiveEvent(client, "foobar"), worker);

        assertThat("component class", meta.componentClass, equalTo(String.class));
        assertTrue("dispatch response is success", resp.isSuccess());
    }

    @Test
    public void testScanSupervise() throws Exception {
        WithSupervise state = new WithSupervise();

        Listener l = b.build(state).collect(MoreCollectors.uniq());
        Events.ComponentWiseEventMetadata meta = (Events.ComponentWiseEventMetadata) l.getHandledEvent();

        Future<Object> resp = l.dispatch(state, new SuperviseEvent(client, new NullPointerException()), worker);

        assertThat("component class", meta.componentClass, equalTo(NullPointerException.class));
        assertTrue("dispatch response is success", resp.isSuccess());
    }

    @Test
    public void testScanError() throws Exception {
        long count = b.build(new Invalids()).count();

        assertThat("scanned listeners", count, equalTo(0L));
    }

    @Test
    public void testScanValidatedFailure() throws Exception {
        WithValidation state = spy(new WithValidation());

        @SuppressWarnings("unchecked")
        MutProp<Object> prop = mock(MutProp.class);
        PropId pid = PropIds.type(String.class);
        when(client.getProp(pid)).thenReturn(prop);
        when(prop.isDefined()).thenReturn(false);

        Object[] tmp = b.build(state).toArray();
        Listener hard = (Listener) tmp[0];
        Listener soft = (Listener) tmp[1];

        Future<Object> hardResp = hard.dispatch(state, new ReceiveEvent(client, "foo"), worker);
        Future<Object> softResp = soft.dispatch(state, new ReceiveEvent(client, "bar"), worker);

        assertTrue("hard response is failure", hardResp.isFailure());
        assertTrue("soft response is success", softResp.isSuccess());

        InOrder o = inOrder(state);
        o.verifyNoMoreInteractions();
    }

    @Test
    public void testScanValidatedSuccess() throws Exception {
        WithValidation state = spy(new WithValidation());

        @SuppressWarnings("unchecked")
        MutProp<Object> prop = mock(MutProp.class);
        PropId pid = PropIds.type(String.class);
        when(client.getProp(pid)).thenReturn(prop);
        when(prop.isDefined()).thenReturn(true);

        Object[] tmp = b.build(state).toArray();
        Listener hard = (Listener) tmp[0];
        Listener soft = (Listener) tmp[1];

        Future<Object> hardResp = hard.dispatch(state, new ReceiveEvent(client, "foo"), worker);
        Future<Object> softResp = soft.dispatch(state, new ReceiveEvent(client, "bar"), worker);

        assertTrue("hard response is failure", hardResp.isSuccess());
        assertTrue("soft response is success", softResp.isSuccess());

        InOrder o = inOrder(state);
        o.verify(state).hard("foo");
        o.verify(state).soft("bar");
        o.verifyNoMoreInteractions();
    }
}