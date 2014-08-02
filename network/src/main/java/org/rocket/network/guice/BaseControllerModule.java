package org.rocket.network.guice;

import com.google.inject.*;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;
import org.rocket.guice.RocketModule;
import org.rocket.network.Controller;
import org.rocket.network.MutProp;
import org.rocket.network.NetworkClient;

import java.lang.annotation.Annotation;
import java.util.Set;

public abstract class BaseControllerModule extends RocketModule {
    @SuppressWarnings("unchecked")
    static final Key<Set<Object>> CONTROLLERS_KEY = (Key<Set<Object>>) Key.get(Types.setOf(Object.class), Controller.class);

    protected abstract Binder controllerBinder();
    protected abstract Multibinder<Object> controllerMultibinder();

    protected LinkedBindingBuilder<Object> newController() {
        return controllerMultibinder().addBinding();
    }

    protected <T> AnnotatedBindingBuilder<T> newHelper(Class<T> klass) {
        return controllerBinder().bind(klass);
    }

    protected <T> LinkedBindingBuilder<T> newHelper(Key<T> key) {
        return controllerBinder().bind(key);
    }

    protected <T> AnnotatedBindingBuilder<T> newHelper(TypeLiteral<T> type) {
        return controllerBinder().bind(type);
    }

    @SuppressWarnings("unchecked")
    protected void newProp(Key<?> key) {
        PropProvider provider = new PropProvider(key);
        controllerBinder().requestInjection(provider);

        bind(RocketGuiceUtil.wrapProp(key)).toProvider(provider);
        bind(RocketGuiceUtil.wrapMutProp(key)).toProvider(provider);
    }

    protected void newProp(Class<?> klass) {
        newProp(Key.get(klass));
    }

    protected void newProp(Class<?> klass, Class<? extends Annotation> annotationType) {
        newProp(Key.get(klass, annotationType));
    }

    protected void newProp(Class<?> klass, Annotation annotation) {
        newProp(Key.get(klass, annotation));
    }

    private static class PropProvider implements Provider<MutProp<?>> {
        private final Key<?> key;
        @Inject Provider<NetworkClient> client;

        private PropProvider(Key<?> key) {
            this.key = key;
        }

        @Override
        public MutProp<?> get() {
            return client.get().getMutProp(key);
        }
    }
}
