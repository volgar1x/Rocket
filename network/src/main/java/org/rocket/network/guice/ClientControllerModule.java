package org.rocket.network.guice;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.PrivateBinder;
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
        controllerBinder.bind(ControllerFactory.class).to(Hook.class);
        controllerBinder.expose(ControllerFactory.class);
    }

    @Override
    protected Multibinder<Object> controllerMultibinder() {
        return controllerMultibinder;
    }

    @Override
    protected Binder controllerBinder() {
        return controllerBinder;
    }

    static class Hook implements ControllerFactory {
        @Inject Injector injector;

        @Override
        public Set<Object> create(NetworkClient client) {
            return injector.getInstance(CONTROLLERS_KEY);
        }
    }
}
