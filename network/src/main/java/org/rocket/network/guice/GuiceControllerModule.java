package org.rocket.network.guice;

import com.google.inject.*;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;
import org.rocket.guice.RocketModule;
import org.rocket.network.Controller;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;

import javax.inject.Inject;
import java.util.Set;

public abstract class GuiceControllerModule extends RocketModule {
    @SuppressWarnings("unchecked")
    private static final Key<Set<Object>> CONTROLLERS_KEY = (Key<Set<Object>>) Key.get(Types.setOf(Object.class), Controller.class);

    private final Key<NetworkClient> clientKey;
    private PrivateBinder theBinder;
    private Multibinder<Object> multibinder;

    public GuiceControllerModule(Key<NetworkClient> clientKey) {
        this.clientKey = clientKey;
    }

    public GuiceControllerModule() {
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
        theBinder = newPrivateBinder();
        multibinder = Multibinder.newSetBinder(theBinder, Object.class, Controller.class);
    }

    @Override
    protected void after() {
        configureControllers();
        multibinder = null;
        theBinder = null;
    }

    protected LinkedBindingBuilder<Object> newController() {
        return multibinder.addBinding();
    }

    protected <T> AnnotatedBindingBuilder<T> newHelper(Class<T> klass) {
        return theBinder.bind(klass);
    }

    protected <T> LinkedBindingBuilder<T> newHelper(Key<T> key) {
        return theBinder.bind(key);
    }

    protected <T> AnnotatedBindingBuilder<T> newHelper(TypeLiteral<T> type) {
        return theBinder.bind(type);
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
