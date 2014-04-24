package org.rocket.network.netty;

import io.netty.channel.Channel;
import org.fungsi.concurrent.Timer;
import org.rocket.network.NetworkClient;
import org.rocket.network.NetworkCommand;
import org.rocket.network.Transactional;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NettyClient implements NetworkClient {
	final Channel channel; // package-private to allow some optimizations
    final Supplier<Timer> timer;

	public NettyClient(Channel channel, Supplier<Timer> timer) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.timer = Objects.requireNonNull(timer, "timer");
    }

    @Override
    public SocketAddress getLocalAddress() {
        return channel.localAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    @Override
	public final NetworkCommand write(Object o) {
		return new WriteCommand(channel, o, timer);
	}

	@Override
	public final NetworkCommand transaction(Consumer<Transactional> fn) {
		return new TransactionCommand(channel, fn, timer);
	}

	@Override
	public final NetworkCommand close() {
		return new CloseCommand(channel, timer);
	}

	@Override
	public String toString() {
		return channel.toString();
	}
}
