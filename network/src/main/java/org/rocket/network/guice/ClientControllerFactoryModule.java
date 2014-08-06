package org.rocket.network.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import org.rocket.network.Controller;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;

import java.lang.annotation.Annotation;
import java.util.Set;

public final class ClientControllerFactoryModule extends AbstractModule {
    private final Class<? extends Annotation> controllerAnnotation;

    public ClientControllerFactoryModule(Class<? extends Annotation> controllerAnnotation) {
        this.controllerAnnotation = controllerAnnotation;
    }

    public ClientControllerFactoryModule() {
        this(Controller.class);
    }

    @Override
    protected void configure() {
        bind(ControllerFactory.class).toInstance(new Hook(getProvider(RocketGuiceUtil.controllersKeyFor(controllerAnnotation))));
    }

    private static class Hook implements ControllerFactory {
        private final Provider<Set<Object>> controllersProvider;

        private Hook(Provider<Set<Object>> controllersProvider) {
            this.controllersProvider = controllersProvider;
        }

        @Override
        public Set<Object> create(NetworkClient client) {
            return controllersProvider.get();
        }
    }
}
