package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public abstract class NetworkEvent {
    private final NetworkClient client;

    protected NetworkEvent(NetworkClient client) {
        this.client = client;
    }

    public NetworkClient getClient() {
        return client;
    }
}
