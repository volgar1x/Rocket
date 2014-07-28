package org.rocket.network.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.util.Types;
import org.rocket.network.Controller;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;

import java.util.Set;

import static com.google.common.collect.ImmutableList.of;

public final class GuiceControllers {
    private GuiceControllers() {}

    @SuppressWarnings("unchecked")
    public static final Key<Set<Object>> CONTROLLERS_KEY = (Key<Set<Object>>) Key.get(Types.setOf(Object.class), Controller.class);

    public static ControllerFactory getFactory(Injector injector, Key<NetworkClient> key) {
        return client -> {
            Injector inj = injector.createChildInjector(of(binder -> binder.bind(key).toInstance(client)));
            return inj.getInstance(CONTROLLERS_KEY);
        };
    }
}
