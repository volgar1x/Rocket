package org.rocket.network.netty;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import org.rocket.network.NetworkClient;
import org.rocket.network.NetworkCommand;
import org.rocket.network.Transactional;

import java.util.Objects;
import java.util.function.Consumer;

public class NettyClient implements NetworkClient {
	final Channel channel; // package-private to allow some optimizations

	public NettyClient(Channel channel) {
		this.channel = Objects.requireNonNull(channel, "channel");
	}

	@Override
	public final NetworkCommand write(Object o) {
		return new WriteCommand(channel, o);
	}

	@Override
	public final NetworkCommand transaction(Consumer<Transactional> fn) {
		return new TransactionCommand(channel, fn);
	}

	@Override
	public final NetworkCommand close() {
		return new CloseCommand(channel);
	}

	@Override
	public final NetworkCommand closeNow() {
		return close();
	}

	@Override
	public String toString() {
		return channel.toString();
	}
}
