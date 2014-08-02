package org.rocket.network;

import com.github.blackrush.acara.EventBus;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;

import java.util.function.Consumer;

public interface NetworkClient extends PropBag {
    EventBus getEventBus();

    Future<Unit> write(Object msg);
    Future<Unit> transaction(Consumer<NetworkTransaction> fn);
    Future<Unit> close();
}
