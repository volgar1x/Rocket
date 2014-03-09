package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public final class ReceiveEvent<C extends NetworkClient> extends NetworkEvent<C> {
	private final Object message;

	public ReceiveEvent(C client, Object message) {
		super(client);
		this.message = message;
	}

	public Object getMessage() {
		return message;
	}
}
