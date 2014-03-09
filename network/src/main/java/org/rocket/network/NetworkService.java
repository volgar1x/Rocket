package org.rocket.network;

import com.google.common.collect.ImmutableSet;
import net.engio.mbassy.bus.IMessageBus;
import org.rocket.Service;
import org.rocket.network.event.NetworkEvent;

public interface NetworkService<C extends NetworkClient> extends Service {
	int getActualConnectedClients();
	int getMaxConnectedClients();

	ImmutableSet<C> getClients();

	NetworkCommand broadcast(Object o);

	IMessageBus<NetworkEvent<C>, ?> getEventBus();
}
