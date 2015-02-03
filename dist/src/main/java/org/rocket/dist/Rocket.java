package org.rocket.dist;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public abstract class Rocket {
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public Config getConfig() {
        return ConfigFactory.load(getClassLoader());
    }

    public Module getModule() {
        return binder -> {};
    }

    public Injector getInjector() {
        return Guice.createInjector(getModule());
    }

}
