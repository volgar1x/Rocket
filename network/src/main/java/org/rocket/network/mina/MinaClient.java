package org.rocket.network.mina;

import org.apache.mina.core.session.IoSession;
import org.fungsi.concurrent.Timer;
import org.rocket.network.NetworkClient;
import org.rocket.network.NetworkCommand;
import org.rocket.network.Transactional;

import java.net.SocketAddress;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class MinaClient implements NetworkClient {
    final IoSession session;
    private final Supplier<Timer> timer;

    protected MinaClient(IoSession session, Supplier<Timer> timer) {
        this.session = session;
        this.timer = timer;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return session.getLocalAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return session.getRemoteAddress();
    }

    @Override
    public NetworkCommand write(Object o) {
        return new WriteCommand(session, o, timer);
    }

    @Override
    public NetworkCommand transaction(Consumer<Transactional> fn) {
        return new TransactionCommand(session, fn, timer);
    }

    @Override
    public NetworkCommand close() {
        return new CloseCommand(session, false, timer);
    }
}
