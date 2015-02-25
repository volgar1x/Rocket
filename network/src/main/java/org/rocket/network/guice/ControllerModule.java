package org.rocket.network.guice;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import org.rocket.guice.RocketModule;
import org.rocket.network.*;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

        for (Field field : klass.getFields()) {
            if (!field.isAnnotationPresent(Inject.class) && !field.isAnnotationPresent(com.google.inject.Inject.class)) {
                continue;
            }

            if (!Prop.class.isAssignableFrom(field.getType())) {
                continue;
            }

            newProp(keyFor(field));
        }
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

    private Key<?> keyFor(Field field) {
        // find the first eligible annotation other than @Inject
        // com.google.inject.Key only supports a single marker annotation
        // a contrario of PropId which can handle any number of annotations
        Annotation acc = null;
        for (Annotation ann : field.getAnnotations()) {
            if (Inject.class.isAssignableFrom(ann.annotationType())) {
                continue;
            }
            if (com.google.inject.Inject.class.isAssignableFrom(ann.annotationType())) {
                continue;
            }

            acc = ann;
            break;
        }

        if (acc != null) {
            return Key.get(field.getGenericType(), acc);
        }

        return Key.get(field.getGenericType());
    }

    private Key<?> wrap(Key<?> key, Class<?> wrapper) {
        throw new Error("todo");
    }

    private PropId asPropId(Key<?> key) {
        throw new Error("todo");
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
