package org.rocket.network.netty;

import com.github.blackrush.acara.EventBus;
import io.netty.channel.Channel;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.rocket.network.NetworkClient;
import org.rocket.network.NetworkTransaction;

import java.util.LinkedList;
import java.util.function.Consumer;

final class NettyClient implements NetworkClient {
    final Channel channel;
    final long id;
    final EventBus eventBus;

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
        return Netty.toFungsi(channel.writeAndFlush(msg)).toUnit();
    }

    @Override
    public Future<Unit> transaction(Consumer<NetworkTransaction> fn) {
        BufTransaction tx = new BufTransaction();
        fn.accept(tx);

        return tx.stream()
                .map(channel::write)
                .map(Netty::toFungsi)
                .collect(Futures.collect())
                .toUnit()
                .onSuccess(x -> channel.flush())
                ;
    }

    @Override
    public Future<Unit> close() {
        return Netty.toFungsi(channel.close()).toUnit();
    }

    class BufTransaction extends LinkedList<Object> implements NetworkTransaction {
        @Override
        public void write(Object msg) {
            add(msg);
        }
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
                "id=" + id +
                ')';
    }
}
