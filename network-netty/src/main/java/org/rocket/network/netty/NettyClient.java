package org.rocket.network.netty;

import com.github.blackrush.acara.EventBus;
import io.netty.channel.Channel;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.rocket.network.NetworkClient;
import org.rocket.network.NetworkTransaction;

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
        channel.writeAndFlush(msg);
        return Futures.unit();
    }

    @Override
    public Future<Unit> transaction(Consumer<NetworkTransaction> fn) {
        fn.accept(channel::write);
        channel.flush();

        return Futures.unit();
    }

    @Override
    public Future<Unit> close() {
        return RocketNetty.toFungsi(channel.close()).toUnit();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != NettyClient.class) return false;

        NettyClient that = (NettyClient) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "NettyClient(" +
                "id=" + id +
                ')';
    }
}
