package org.rocket.network;

import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.rocket.Service;

public interface NetworkService extends Service {
    int getActualConnectedClients();
	int getMaxConnectedClients();

    Future<Unit> broadcast(Object msg);
}
