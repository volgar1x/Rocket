package org.rocket.network;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Receive {
    public final class Event {
        private final NetworkClient client;
        private final Object message;

        public Event(NetworkClient client, Object message) {
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
}
