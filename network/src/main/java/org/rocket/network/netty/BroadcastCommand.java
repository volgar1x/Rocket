package org.rocket.network.netty;

import io.netty.channel.Channel;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.fungsi.concurrent.Timer;
import org.rocket.network.NetworkCommand;

import java.time.Duration;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class BroadcastCommand implements NetworkCommand {
    private final Stream<Channel> clients;
	private final Object o;
    private final Supplier<Timer> timer;

	public BroadcastCommand(Stream<Channel> clients, Object o, Supplier<Timer> timer) {
        this.clients = clients;
		this.o = o;
        this.timer = timer;
    }

	@Override
	public void now() {
		clients.map(x -> x.writeAndFlush(o))
                .map(ChannelFutures::toFungsi)
                .collect(Futures.collect())
                .get();
	}

	@Override
	public void now(Duration max) {
		clients.map(x -> x.writeAndFlush(o))
                .map(ChannelFutures::toFungsi)
                .collect(Futures.collect())
                .get(max);
	}

	@Override
	public Future<Unit> async() {
        return clients.map(x -> x.writeAndFlush(o))
                .map(ChannelFutures::toFungsi)
                .collect(Futures.collect())
                .toUnit();
	}

	@Override
	public Future<Unit> async(Duration max) {
		return async().within(max, timer.get());
	}
}
