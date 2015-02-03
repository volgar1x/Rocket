package org.rocket.network.event;

import org.rocket.network.NetworkClient;

public final class SuperviseEvent extends NetworkEvent {
    private final Throwable exception;

    public SuperviseEvent(NetworkClient client, Throwable exception) {
        super(client);
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }
}
