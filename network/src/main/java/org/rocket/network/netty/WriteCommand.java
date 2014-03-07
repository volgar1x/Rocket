package org.rocket.network.netty;

import io.netty.channel.socket.SocketChannel;
import org.rocket.network.NetworkCommand;

import java.time.Duration;
import java.util.Objects;

public final class WriteCommand implements NetworkCommand {
	private final SocketChannel channel;
	private final Object o;

	public WriteCommand(SocketChannel channel, Object o) {
		this.channel = Objects.requireNonNull(channel, "channel");
		this.o = o;
	}

	@Override
	public void now() {
		channel.write(o).awaitUninterruptibly();
	}

	@Override
	public void now(Duration max) {
		channel.write(o).awaitUninterruptibly(max.toMillis());
	}

	@Override
	public void async() {
		channel.write(o);
	}

	@Override
	public void async(Duration max) { // not supported
		async();
	}
}
