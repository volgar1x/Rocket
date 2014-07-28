package org.rocket.network;

import com.github.blackrush.acara.EventBus;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.rocket.Service;

public interface NetworkService extends Service {
    EventBus getEventBus();

    int getActualConnectedClients();
	int getMaxConnectedClients();

    Future<Unit> broadcast(Object msg);
}
