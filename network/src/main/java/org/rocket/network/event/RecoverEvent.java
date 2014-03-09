package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public final class RecoverEvent<C extends NetworkClient> extends NetworkEvent<C> {
	private final Throwable error;

	public RecoverEvent(C client, Throwable error) {
		super(client);
		this.error = error;
	}

	public Throwable getError() {
		return error;
	}
}
