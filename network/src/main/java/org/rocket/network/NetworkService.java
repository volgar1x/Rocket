package org.rocket.network;

import com.google.common.collect.ImmutableSet;
import net.engio.mbassy.bus.IMessageBus;
import org.rocket.Service;
import org.rocket.network.event.NetworkEvent;

import java.util.stream.Stream;

public interface NetworkService<C extends NetworkClient> extends Service {
	int getActualConnectedClients();
	int getMaxConnectedClients();

	ImmutableSet<C> getClients();

	NetworkCommand broadcast(Stream<C> clients, Object o);
	default NetworkCommand broadcast(Object o) {
		return broadcast(getClients().stream(), o);
	}

	IMessageBus<NetworkEvent<C>, ?> getEventBus();
}
