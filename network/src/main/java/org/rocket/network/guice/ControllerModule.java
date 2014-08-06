package org.rocket.network.guice;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import org.rocket.guice.RocketModule;
import org.rocket.network.Controller;
import org.rocket.network.MutProp;
import org.rocket.network.NetworkClient;

import java.lang.annotation.Annotation;

public abstract class ControllerModule extends RocketModule {

    private Multibinder<Object> controllerMultibinder;

    @Override
    protected void before() {
        controllerMultibinder = Multibinder.newSetBinder(binder(), Object.class, Controller.class);
    }

    @Override
    protected void after() {
        controllerMultibinder = null;
    }

    protected LinkedBindingBuilder<Object> newController() {
        return controllerMultibinder.addBinding();
    }

    protected void newProp(Key<?> key) {
        PropProvider provider = new PropProvider(key, getProvider(NetworkClient.class));
        bind(RocketGuiceUtil.wrapProp(key)).toProvider(provider);
        bind(RocketGuiceUtil.wrapMutProp(key)).toProvider(provider);
    }

    protected void newProp(Class<?> klass) {
        newProp(Key.get(klass));
    }

    protected void newProp(Class<?> klass, Class<? extends Annotation> annotationClass) {
        newProp(Key.get(klass, annotationClass));
    }

    protected void newProp(Class<?> klass, Annotation annotation) {
        newProp(Key.get(klass, annotation));
    }

    private static class PropProvider implements Provider<MutProp<?>> {
        private final Key<?> key;
        private final Provider<NetworkClient> client;

        private PropProvider(Key<?> key, Provider<NetworkClient> client) {
            this.key = key;
            this.client = client;
        }

        @Override
        public MutProp<?> get() {
            return client.get().getMutProp(key);
        }
    }
}
