package org.rocket.network.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
		ChannelFuture last;

		@Override
		public void write(Object o) {
			last = channel.write(o);
		}

        Future<Unit> chained() {
            return last != null
                ? ChannelFutures.toFungsi(last)
                : Futures.unit()
                ;
        }
	}

	@Override
	public void now() {
		TransactionalImpl impl = new TransactionalImpl();
		transaction.accept(impl);
		channel.flush();

        impl.chained().get();
	}

	@Override
	public void now(Duration max) {
		TransactionalImpl impl = new TransactionalImpl();
		transaction.accept(impl);
		channel.flush();

        impl.chained().get(max);
	}

	@Override
	public Future<Unit> async() {
        TransactionalImpl impl = new TransactionalImpl();
        transaction.accept(impl);
        channel.flush();

        return impl.chained();
	}

	@Override
	public Future<Unit> async(Duration max) {
		return async().within(max, timer.get());
	}
}
