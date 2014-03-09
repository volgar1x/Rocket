package org.rocket.network.netty;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import org.rocket.network.NetworkCommand;

import java.time.Duration;
import java.util.Objects;

public final class WriteCommand implements NetworkCommand {
	private final Channel channel;
	private final Object o;

	public WriteCommand(Channel channel, Object o) {
		this.channel = Objects.requireNonNull(channel, "channel");
		this.o = o;
	}

	@Override
	public void now() {
		channel.writeAndFlush(o).awaitUninterruptibly();
	}

	@Override
	public void now(Duration max) {
		channel.writeAndFlush(o).awaitUninterruptibly(max.toMillis());
	}

	@Override
	public void async() {
		channel.writeAndFlush(o);
	}

	@Override
	public void async(Duration max) { // not supported
		async();
	}
}
