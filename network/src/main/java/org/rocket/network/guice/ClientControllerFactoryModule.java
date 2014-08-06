package org.rocket.network.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.rocket.network.Controller;
import org.rocket.network.ControllerFactory;

import java.util.Set;

public final class ClientControllerFactoryModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    ControllerFactory provideControllerFactory(@Controller Set<Object> controllers) {
        return client -> controllers;
    }
}
