package org.rocket.network.guice;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;
import org.rocket.guice.RocketModule;
import org.rocket.network.*;
import org.rocket.network.props.PropIds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public abstract class ControllerModule extends RocketModule {

    private final Class<? extends Annotation> controllerAnnotation;

    private Multibinder<Object> controllerMultibinder;
    private final Set<Key<?>> boundPropKeys = new HashSet<>();

    protected ControllerModule(Class<? extends Annotation> controllerAnnotation) {
        this.controllerAnnotation = requireNonNull(controllerAnnotation, "controllerAnnotation");
    }

    protected ControllerModule() {
        this(Controller.class);
    }

    @Override
    protected void before() {
        controllerMultibinder = Multibinder.newSetBinder(binder(), Object.class, controllerAnnotation);
    }

    @Override
    protected void after() {
        controllerMultibinder = null;
    }

    public interface Builder {
        void to(Class<?> klass);
    }
    protected Builder newController() {
        return this::newController;
    }

    protected void newController(Class<?> klass) {
        controllerMultibinder.addBinding().to(klass);
    }

    @SuppressWarnings("unchecked")
    protected void newProp(Key<?> key) {
        if (boundPropKeys.contains(key)) {
            return;
        }
        boundPropKeys.add(key);

        Provider provider = new PropProvider(getProvider(NetworkClient.class), asPropId(key));
        bind(wrap(key, Prop.class)).toProvider(provider);
        bind(wrap(key, MutProp.class)).toProvider(provider);
    }

    protected void newProp(Class<?> klass) {
        newProp(Key.get(klass));
    }

    protected void newProp(Class<?> klass, Annotation ann) {
        newProp(Key.get(klass, ann));
    }

    protected void newProp(Class<?> klass, Class<? extends Annotation> ann) {
        newProp(Key.get(klass, ann));
    }

    private Key<?> wrap(Key<?> key, Type wrapper) {
        return key.ofType(Types.newParameterizedType(wrapper, key.getTypeLiteral().getType()));
    }

    private PropId asPropId(Key<?> key) {
        if (key.getAnnotation() == null) {
            return PropIds.type(key.getTypeLiteral().getType());
        }
        return PropIds.of(
            PropIds.type(key.getTypeLiteral().getType()),
            PropIds.annotation(key.getAnnotation())
        );
    }

    private static class PropProvider implements Provider {
        private final Provider<NetworkClient> client;
        private final PropId pid;

        private PropProvider(Provider<NetworkClient> client, PropId pid) {
            this.client = client;
            this.pid = pid;
        }

        @Override
        public Object get() {
            return client.get().getProp(pid);
        }
    }
}
