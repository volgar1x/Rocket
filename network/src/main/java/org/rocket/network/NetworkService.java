package org.rocket.network;

import com.github.blackrush.acara.EventBus;
import com.google.common.collect.ImmutableSet;
import org.rocket.Service;

import java.util.stream.Stream;

public interface NetworkService<C extends NetworkClient> extends Service {
	int getActualConnectedClients();
	int getMaxConnectedClients();

	ImmutableSet<C> getClients();

	NetworkCommand broadcast(Stream<C> clients, Object o);
	default NetworkCommand broadcast(Object o) {
		return broadcast(getClients().stream(), o);
	}

	EventBus getEventBus();
}
