package org.rocket.network.guice;

import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import org.rocket.guice.RocketModule;
import org.rocket.network.Controller;

public abstract class GuiceControllerModule extends RocketModule {
    private Multibinder<Object> multibinder;

    @Override
    protected void before() {
        multibinder = Multibinder.newSetBinder(binder(), Object.class, Controller.class);
    }

    @Override
    protected void after() {
        multibinder = null;
    }

    protected LinkedBindingBuilder<Object> newController() {
        return multibinder.addBinding();
    }
}
