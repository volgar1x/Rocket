package org.rocket.network.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;
import org.rocket.network.NetworkCommand;
import org.rocket.network.Transactional;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

public final class TransactionCommand implements NetworkCommand {
	private final Channel channel;
	private final Consumer<Transactional> transaction;

	public TransactionCommand(Channel channel, Consumer<Transactional> transaction) {
		this.channel = Objects.requireNonNull(channel, "channel");
		this.transaction = transaction;
	}

	class TransactionalImpl implements Transactional {
		ChannelFuture last;

		@Override
		public void write(Object o) {
			last = channel.write(o);
		}
	}

	@Override
	public void now() {
		TransactionalImpl impl = new TransactionalImpl();
		transaction.accept(impl);
		channel.flush();

		if (impl.last != null) {
			impl.last.awaitUninterruptibly();
		}
	}

	@Override
	public void now(Duration max) {
		TransactionalImpl impl = new TransactionalImpl();
		transaction.accept(impl);
		channel.flush();

		if (impl.last != null) {
			impl.last.awaitUninterruptibly(max.toMillis());
		}
	}

	@Override
	public void async() {
		transaction.accept(new TransactionalImpl());
		channel.flush();
	}

	@Override
	public void async(Duration max) {
		async();
	}
}
