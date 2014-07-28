package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public final class ConnectEvent {
    private final NetworkClient client;
    private final boolean disconnecting;

    public ConnectEvent(NetworkClient client, boolean disconnecting) {
        this.client = client;
        this.disconnecting = disconnecting;
    }

    public NetworkClient getClient() {
        return client;
    }

    public boolean isDisconnecting() {
        return disconnecting;
    }
}
