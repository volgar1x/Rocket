package org.rocket.network.mina;

import org.apache.mina.core.session.IoSession;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.fungsi.concurrent.Timer;
import org.rocket.network.NetworkCommand;
import org.rocket.network.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Apache Mina doesn't provide any way to prepare a list of write and then commit them all
 * Under the hood, this TransactionCommand buffers all written message inside an ArrayList
 * and then write them all.
 */
public final class TransactionCommand implements NetworkCommand {
    private final IoSession session;
    private final Consumer<Transactional> tx;
    private final Supplier<Timer> timer;

    public TransactionCommand(IoSession session, Consumer<Transactional> tx, Supplier<Timer> timer) {
        this.session = session;
        this.tx = tx;
        this.timer = timer;
    }

    class TxImpl implements Transactional {
        List<Object> messages = new ArrayList<>();

        @Override
        public void write(Object o) {
            messages.add(o);
        }

        Future<Unit> acc() {
            return messages.stream()
                    .map(session::write)
                    .map(MinaUtils::toFungsi)
                    .collect(Futures.collect())
                    .toUnit();
        }
    }

    @Override
    public void now() {
        TxImpl impl = new TxImpl();
        tx.accept(impl);
        Future<Unit> fut = impl.acc();
        fut.get();
    }

    @Override
    public void now(Duration max) {
        TxImpl impl = new TxImpl();
        tx.accept(impl);
        Future<Unit> fut = impl.acc();
        fut.get(max);
    }

    @Override
    public Future<Unit> async() {
        TxImpl impl = new TxImpl();
        tx.accept(impl);
        return impl.acc();
    }

    @Override
    public Future<Unit> async(Duration max) {
        return async().within(max, timer.get());
    }
}
