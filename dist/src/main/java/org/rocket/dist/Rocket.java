package org.rocket.dist;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.rocket.ImmutableServiceContext;
import org.rocket.ServiceContext;

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

    public final ServiceContext getServiceContext() {
        return ImmutableServiceContext.of(getConfig(), getClassLoader(), getInjector());
    }
}
