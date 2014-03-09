package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public final class DisconnectEvent<C extends NetworkClient> extends NetworkEvent<C> {
	public DisconnectEvent(C client) {
		super(client);
	}
}
