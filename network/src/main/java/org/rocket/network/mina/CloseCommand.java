package org.rocket.network.mina;

import org.apache.mina.core.session.IoSession;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Timer;
import org.rocket.network.NetworkCommand;

import java.time.Duration;
import java.util.function.Supplier;

import static org.rocket.network.mina.MinaUtils.toFungsi;

public final class CloseCommand implements NetworkCommand {
    private final IoSession session;
    private final boolean flush;
    private final Supplier<Timer> timer;

    public CloseCommand(IoSession session, boolean flush, Supplier<Timer> timer) {
        this.session = session;
        this.flush = flush;
        this.timer = timer;
    }

    @Override
    public void now() {
        session.close(!flush).awaitUninterruptibly();
    }

    @Override
    public void now(Duration max) {
        session.close(!flush).awaitUninterruptibly(max.toMillis());
    }

    @Override
    public Future<Unit> async() {
        return toFungsi(session.close(!flush));
    }

    @Override
    public Future<Unit> async(Duration max) {
        return async().within(max, timer.get());
    }
}
