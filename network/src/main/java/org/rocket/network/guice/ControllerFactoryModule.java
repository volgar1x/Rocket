package org.rocket.network.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.rocket.network.Controller;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;

import java.util.Set;

public final class ControllerFactoryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Hook.class).asEagerSingleton();
        bind(NetworkClient.class).toProvider(Hook.class);
        bind(ControllerFactory.class).to(Hook.class);
    }

    private static class Hook implements Provider<NetworkClient>, ControllerFactory {
        private final ThreadLocal<NetworkClient> client = new ThreadLocal<>();
        private final Provider<Set<Object>> controllers;

        @Inject
        Hook(@Controller Provider<Set<Object>> controllers) {
            this.controllers = controllers;
        }

        @Override
        public NetworkClient get() {
            return client.get();
        }

        @Override
        public Set<Object> create(NetworkClient client) {
            this.client.set(client);
            try {
                return controllers.get();
            } finally {
                this.client.remove();
            }
        }
    }
}
