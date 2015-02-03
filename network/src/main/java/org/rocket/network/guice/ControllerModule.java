package org.rocket.network.guice;

import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import org.rocket.guice.RocketModule;
import org.rocket.network.Controller;

import java.lang.annotation.Annotation;

import static java.util.Objects.requireNonNull;

public abstract class ControllerModule extends RocketModule {

    private final Class<? extends Annotation> controllerAnnotation;

    private Multibinder<Object> controllerMultibinder;

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

    protected LinkedBindingBuilder<Object> newController() {
        return controllerMultibinder.addBinding();
    }
}
