package org.rocket.network.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import org.rocket.network.Controller;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;

import java.lang.annotation.Annotation;
import java.util.Set;

public final class ControllerFactoryModule extends AbstractModule {
    private final Class<? extends Annotation> controllerAnnotation;

    public ControllerFactoryModule(Class<? extends Annotation> controllerAnnotation) {
        this.controllerAnnotation = controllerAnnotation;
    }

    public ControllerFactoryModule() {
        this(Controller.class);
    }

    @Override
    protected void configure() {
        Hook hook = new Hook(getProvider(RocketGuiceUtil.controllersKeyFor(controllerAnnotation)));
        bind(NetworkClient.class).toProvider(hook);
        bind(ControllerFactory.class).toInstance(hook);
    }

    private static class Hook implements Provider<NetworkClient>, ControllerFactory {
        private final ThreadLocal<NetworkClient> client = new ThreadLocal<>();
        private final Provider<Set<Object>> controllers;

        Hook(Provider<Set<Object>> controllers) {
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
