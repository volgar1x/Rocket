package org.rocket.network;

import org.fungsi.Unit;
import org.fungsi.concurrent.Future;

import java.net.SocketAddress;
import java.util.function.Consumer;

public interface NetworkClient {
    SocketAddress getLocalAddress();
    SocketAddress getRemoteAddress();

	NetworkCommand write(Object o);
	NetworkCommand transaction(Consumer<Transactional> fn);
	NetworkCommand close();

    default void send(Object o) {
        write(o).now();
    }

    default Future<Unit> cast(Object o) {
        return write(o).async();
    }

    default void transact(Consumer<Transactional> fn) {
        transaction(fn).now();
    }

    default Future<Unit> commit(Consumer<Transactional> fn) {
        return transaction(fn).async();
    }

    default void kickNow() {
        close().now();
    }

    default Future<Unit> kick() {
        return close().async();
    }
}
