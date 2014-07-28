package org.rocket.network;

import org.fungsi.Unit;
import org.fungsi.concurrent.Future;

import java.util.function.Consumer;

public interface NetworkClient {
    Future<Unit> write(Object msg);
    Future<Unit> transaction(Consumer<NetworkTransaction> fn);
    Future<Unit> close();
}
