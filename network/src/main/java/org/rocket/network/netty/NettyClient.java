package org.rocket.network.netty;

import com.github.blackrush.acara.EventBus;
import com.google.inject.Key;
import io.netty.channel.Channel;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.rocket.network.*;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class NettyClient implements NetworkClient {
    final Channel channel;
    final long id;
    final EventBus eventBus;
    final HashPropBag props = new HashPropBag();

    NettyClient(Channel channel, long id, EventBus eventBus) {
        this.channel = channel;
        this.id = id;
        this.eventBus = eventBus;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public Future<Unit> write(Object msg) {
        channel.writeAndFlush(msg);
        return Futures.unit();
    }

    @Override
    public Future<Unit> transaction(Consumer<NetworkTransaction> fn) {
        BufTransaction tx = new BufTransaction();
        fn.accept(tx);

        tx.forEach(channel::write);
        channel.flush();

        return Futures.unit();
    }

    @Override
    public Future<Unit> close() {
        return RocketNetty.toFungsi(channel.close()).toUnit();
    }

    class BufTransaction extends LinkedList<Object> implements NetworkTransaction {
        @Override
        public void write(Object msg) {
            add(msg);
        }
    }

    @Override
    public <T> Prop<T> getProp(Key<?> key) {
        return props.getProp(key);
    }

    @Override
    public <T> MutProp<T> getMutProp(Key<?> key) {
        return props.getMutProp(key);
    }

    @Override
    public Stream<Key<?>> getPresentPropKeys() {
        return props.getPresentPropKeys();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NettyClient)) return false;

        NettyClient that = (NettyClient) o;
        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "NettyClient(" +
                "id=" + id + "," +
                "nr-props=" + props.getNrPresentProps() +
                ')';
    }
}
