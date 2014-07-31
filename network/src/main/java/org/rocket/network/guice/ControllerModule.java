package org.rocket.network.guice;

import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import org.rocket.network.Controller;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;

import javax.inject.Inject;
import java.util.Set;

public abstract class ControllerModule extends BaseControllerModule {

    private final Key<NetworkClient> clientKey;
    private PrivateBinder theBinder;
    private Multibinder<Object> multibinder;

    public ControllerModule(Key<NetworkClient> clientKey) {
        this.clientKey = clientKey;
    }

    public ControllerModule() {
        this(Key.get(NetworkClient.class));
    }

    private void configureControllers() {
        theBinder.bind(Hook.class).asEagerSingleton();
        theBinder.bind(clientKey).toProvider(Hook.class);
        theBinder.bind(ControllerFactory.class).to(Hook.class);
        theBinder.expose(ControllerFactory.class);
    }

    @Override
    protected void before() {
        theBinder = binder().newPrivateBinder();
        multibinder = Multibinder.newSetBinder(theBinder, Object.class, Controller.class);
    }

    @Override
    protected void after() {
        configureControllers();
        multibinder = null;
        theBinder = null;
    }

    @Override
    protected Binder controllerBinder() {
        return theBinder;
    }

    @Override
    protected Multibinder<Object> controllerMultibinder() {
        return multibinder;
    }

    private static class Hook implements Provider<NetworkClient>, ControllerFactory {
        @Inject private Injector injector;
        final ThreadLocal<NetworkClient> client = new ThreadLocal<>();

        @Override
        public NetworkClient get() {
            return client.get();
        }

        @Override
        public synchronized Set<Object> create(NetworkClient client) {
            this.client.set(client);
            try {
                return injector.getInstance(CONTROLLERS_KEY);
            } finally {
                this.client.remove();
            }
        }
    }
}
