package org.rocket.network.mina;

import org.apache.mina.core.session.IoSession;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Timer;
import org.rocket.network.NetworkCommand;

import java.time.Duration;
import java.util.function.Supplier;

import static org.rocket.network.mina.MinaUtils.toFungsi;

public final class WriteCommand implements NetworkCommand {
    private final IoSession session;
    private final Object o;
    private final Supplier<Timer> timer;

    public WriteCommand(IoSession session, Object o, Supplier<Timer> timer) {
        this.session = session;
        this.o = o;
        this.timer = timer;
    }

    @Override
    public void now() {
        session.write(o).awaitUninterruptibly();
    }

    @Override
    public void now(Duration max) {
        session.write(o).awaitUninterruptibly(max.toMillis());
    }

    @Override
    public Future<Unit> async() {
        return toFungsi(session.write(o));
    }

    @Override
    public Future<Unit> async(Duration max) {
        return async().within(max, timer.get());
    }
}
