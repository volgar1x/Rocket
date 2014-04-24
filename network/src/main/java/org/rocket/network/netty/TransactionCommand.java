package org.rocket.network.netty;

import io.netty.channel.Channel;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.fungsi.concurrent.Timer;
import org.rocket.network.NetworkCommand;
import org.rocket.network.Transactional;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.rocket.network.netty.ChannelFutures.toFungsi;

public final class TransactionCommand implements NetworkCommand {
	private final Channel channel;
	private final Consumer<Transactional> transaction;
    private final Supplier<Timer> timer;

	public TransactionCommand(Channel channel, Consumer<Transactional> transaction, Supplier<Timer> timer) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.transaction = transaction;
        this.timer = timer;
    }

	class TransactionalImpl implements Transactional {
		Future<Unit> acc = Futures.unit();

		@Override
		public void write(Object o) {
			acc = acc.bind(it -> toFungsi(channel.write(o)));
		}
	}

	@Override
	public void now() {
		TransactionalImpl impl = new TransactionalImpl();
		transaction.accept(impl);
		channel.flush();

        impl.acc.get();
	}

	@Override
	public void now(Duration max) {
		TransactionalImpl impl = new TransactionalImpl();
		transaction.accept(impl);
		channel.flush();

        impl.acc.get(max);
	}

	@Override
	public Future<Unit> async() {
        TransactionalImpl impl = new TransactionalImpl();
        transaction.accept(impl);
        channel.flush();

        return impl.acc;
	}

	@Override
	public Future<Unit> async(Duration max) {
		return async().within(max, timer.get());
	}
}
