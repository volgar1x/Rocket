package org.rocket.network.netty;

import io.netty.util.concurrent.EventExecutor;
import org.rocket.network.NetworkCommand;

import java.time.Duration;
import java.util.stream.Stream;

public final class BroadcastCommand implements NetworkCommand {
	private final EventExecutor worker;
	private final Stream<? extends NettyClient> clients;
	private final Object o;

	public BroadcastCommand(EventExecutor worker, Stream<? extends NettyClient> clients, Object o) {
		this.worker = worker;
		this.clients = clients;
		this.o = o;
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
	public void async() {
		clients.forEach(x -> x.channel.writeAndFlush(o));
	}

	@Override
	public void async(Duration max) {
		async();
	}
}
