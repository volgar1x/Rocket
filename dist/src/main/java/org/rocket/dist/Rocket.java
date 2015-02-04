package org.rocket.dist;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public abstract class Rocket {

    public Config getConfig() {
        return ConfigFactory.load(getClass().getClassLoader());
    }

    public Module getModule() {
        return binder -> {};
    }

    public final Injector createInjector() {
        return Guice.createInjector(getModule());
    }

    public Rocket concat(Rocket other) {
        Config config = this.getConfig().withFallback(other.getConfig());
        Module module = Modules.combine(this.getModule(), other.getModule());

        return new Rocket() {
            @Override public Config getConfig() { return config; }
            @Override public Module getModule() { return module; }
        };
    }

}
