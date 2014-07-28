package org.rocket.network;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Connect {
    public final class Event {
        private final NetworkClient client;

        public Event(NetworkClient client) {
            this.client = client;
        }

        public NetworkClient getClient() {
            return client;
        }
    }
}
