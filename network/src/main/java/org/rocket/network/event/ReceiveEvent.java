package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public final class ReceiveEvent extends NetworkEvent {
    private final Object message;

    public ReceiveEvent(NetworkClient client, Object message) {
        super(client);
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }
}
