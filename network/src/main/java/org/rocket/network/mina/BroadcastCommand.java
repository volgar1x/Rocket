package org.rocket.network.mina;

import org.apache.mina.core.session.IoSession;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.fungsi.concurrent.Timer;
import org.rocket.network.NetworkCommand;

import java.time.Duration;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class BroadcastCommand implements NetworkCommand {
    private final Stream<IoSession> sessions;
    private final Object o;
    private final Supplier<Timer> timer;

    public BroadcastCommand(Stream<IoSession> sessions, Object o, Supplier<Timer> timer) {
        this.sessions = sessions;
        this.o = o;
        this.timer = timer;
    }

    @Override
    public Future<Unit> async() {
        return sessions.map(s -> s.write(o))
                .map(MinaUtils::toFungsi)
                .collect(Futures.collect())
                .toUnit()
                ;
    }

    @Override
    public void now() {
        async().get();
    }

    @Override
    public void now(Duration max) {
        async().get(max);
    }

    @Override
    public Future<Unit> async(Duration max) {
        return async().within(max, timer.get());
    }
}
