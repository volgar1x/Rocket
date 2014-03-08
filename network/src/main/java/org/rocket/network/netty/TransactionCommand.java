package org.rocket.network.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;
import org.rocket.network.NetworkCommand;
import org.rocket.network.Transactional;

import java.time.Duration;
import java.util.function.Consumer;

public final class TransactionCommand implements NetworkCommand {
	private final SocketChannel channel;
	private final Consumer<Transactional> transaction;

	public TransactionCommand(SocketChannel channel, Consumer<Transactional> transaction) {
		this.channel = channel;
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
