package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public final class ConnectEvent extends NetworkEvent {
    private final boolean disconnecting;

    public ConnectEvent(NetworkClient client, boolean disconnecting) {
        super(client);
        this.disconnecting = disconnecting;
    }

    public boolean isDisconnecting() {
        return disconnecting;
    }
}
