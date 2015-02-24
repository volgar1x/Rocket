package org.rocket.network.guice;

import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import org.rocket.guice.RocketModule;
import org.rocket.network.Controller;
import org.rocket.network.Prop;

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

    protected void newProp(Key<?> key) {
        if (boundPropKeys.contains(key)) {
            return;
        }
        boundPropKeys.add(key);

        throw new Error("todo");
    }

    private Key<?> keyFor(Field field) {
        throw new Error("todo");
    }

    private Key<?> wrap(Key<?> key, Class<?> wrapper) {
        throw new Error("todo");
    }

}
