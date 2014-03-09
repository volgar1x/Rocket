package org.rocket.network.netty;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import org.rocket.network.NetworkCommand;

import java.time.Duration;
import java.util.Objects;

public final class CloseCommand implements NetworkCommand {
	private final Channel channel;

	public CloseCommand(Channel channel) {
		this.channel = Objects.requireNonNull(channel, "channel");
	}

	@Override
	public void now() {
		channel.close().awaitUninterruptibly();
	}

	@Override
	public void now(Duration max) {
		channel.close().awaitUninterruptibly(max.toMillis());
	}

	@Override
	public void async() {
		channel.close();
	}

	@Override
	public void async(Duration max) { // not supported
		async();
	}
}
