package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public final class ReceiveEvent {
    private final NetworkClient client;
    private final Object message;

    public ReceiveEvent(NetworkClient client, Object message) {
        this.client = client;
        this.message = message;
    }

    public NetworkClient getClient() {
        return client;
    }

    public Object getMessage() {
        return message;
    }
}
