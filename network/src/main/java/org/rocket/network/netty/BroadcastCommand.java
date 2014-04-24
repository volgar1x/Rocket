package org.rocket.network.netty;

import io.netty.util.concurrent.EventExecutor;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.fungsi.concurrent.Timer;
import org.rocket.network.NetworkCommand;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BroadcastCommand implements NetworkCommand {
	private final EventExecutor worker;
	private final Stream<? extends NettyClient> clients;
	private final Object o;
    private final Supplier<Timer> timer;

	public BroadcastCommand(EventExecutor worker, Stream<? extends NettyClient> clients, Object o, Supplier<Timer> timer) {
		this.worker = worker;
		this.clients = clients;
		this.o = o;
        this.timer = timer;
    }

	@Override
	public void now() {
		clients.map(x -> x.channel)
			   .collect(NettyCollectors.collect(worker, x -> x.writeAndFlush(o)))
			   .awaitUninterruptibly();
	}

	@Override
	public void now(Duration max) {
		clients.map(x -> x.channel)
			   .collect(NettyCollectors.collect(worker, x -> x.writeAndFlush(o)))
			   .awaitUninterruptibly(max.toMillis());
	}

	@Override
	public Future<Unit> async() {
        List<Future<Unit>> futures = clients
                .map(x -> x.channel.writeAndFlush(o))
                .map(ChannelFutures::toFungsi)
                .collect(Collectors.toList());

        return Futures.collect(futures).map(it -> Unit.instance());
	}

	@Override
	public Future<Unit> async(Duration max) {
		return async().within(max, timer.get());
	}
}
