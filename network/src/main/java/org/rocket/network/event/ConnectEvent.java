package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public final class ConnectEvent<C extends NetworkClient> extends NetworkEvent<C> {
	public ConnectEvent(C client) {
		super(client);
	}
}
