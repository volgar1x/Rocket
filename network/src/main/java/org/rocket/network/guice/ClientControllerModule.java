package org.rocket.network.guice;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.PrivateBinder;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import org.rocket.network.Controller;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;

import java.util.Set;

public abstract class ClientControllerModule extends BaseControllerModule {

    private PrivateBinder controllerBinder;
    private Multibinder<Object> controllerMultibinder;

    @Override
    protected void before() {
        controllerBinder = binder().newPrivateBinder();
        controllerMultibinder = Multibinder.newSetBinder(controllerBinder, Object.class, Controller.class);
    }

    @Override
    protected void after() {
        controllerBinder.bind(ControllerFactory.class).to(TheFactory.class);
        controllerBinder.expose(ControllerFactory.class);

        controllerMultibinder = null;
        controllerBinder = null;
    }

    @Override
    protected Binder controllerBinder() {
        return controllerBinder;
    }

    @Override
    protected Multibinder<Object> controllerMultibinder() {
        return controllerMultibinder;
    }

    static class TheFactory implements ControllerFactory {
        private final Provider<Set<Object>> controllers;

        @Inject
        TheFactory(@Controller Provider<Set<Object>> controllers) {
            this.controllers = controllers;
        }

        @Override
        public Set<Object> create(NetworkClient client) {
            return controllers.get();
        }
    }
}
