package org.rocket.network.netty;

import io.netty.channel.socket.SocketChannel;
import org.rocket.network.NetworkClient;
import org.rocket.network.NetworkCommand;

import java.util.Objects;

public class NettyClient implements NetworkClient {
	private final SocketChannel channel;

	public NettyClient(SocketChannel channel) {
		this.channel = Objects.requireNonNull(channel, "channel");
	}

	@Override
	public NetworkCommand write(Object o) {
		return new WriteCommand(channel, o);
	}

	@Override
	public NetworkCommand close() {
		return new CloseCommand(channel);
	}

	@Override
	public NetworkCommand closeNow() { // not supported
		return close();
	}
}
