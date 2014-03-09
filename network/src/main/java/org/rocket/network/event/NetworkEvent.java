package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public abstract class NetworkEvent<C extends NetworkClient> {
	private final C client;

	protected NetworkEvent(C client) {
		this.client = client;
	}

	public C getClient() {
		return client;
	}
}
