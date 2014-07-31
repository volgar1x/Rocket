package org.rocket.network.guice;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;
import org.rocket.guice.RocketModule;
import org.rocket.network.Controller;

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
}
