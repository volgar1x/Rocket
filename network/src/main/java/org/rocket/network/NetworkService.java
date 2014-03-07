package org.rocket.network;

import com.google.common.collect.ImmutableSet;
import org.rocket.Service;

public interface NetworkService<C extends NetworkClient> extends Service {
	int getActualConnectedClients();
	int getMaxConnectedClients();

	ImmutableSet<C> getClients();

	NetworkCommand broadcast(Object o);
}
