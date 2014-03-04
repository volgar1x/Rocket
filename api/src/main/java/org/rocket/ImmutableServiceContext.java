package org.rocket;

import com.google.inject.Injector;
import com.typesafe.config.Config;

import java.util.function.UnaryOperator;

public final class ImmutableServiceContext implements ServiceContext {
	private final Config config;
	private final ClassLoader classLoader;
	private final Injector injector;

	private ImmutableServiceContext(Config config, ClassLoader classLoader, Injector injector) {
		this.config = config;
		this.classLoader = classLoader;
		this.injector = injector;
	}

	public static ImmutableServiceContext of(Config config, ClassLoader classLoader, Injector injector) {
		return new ImmutableServiceContext(config, classLoader, injector);
	}

	@Override
	public Config getConfig() {
		return config;
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public Injector getInjector() {
		return injector;
	}

	public ImmutableServiceContext withConfig(UnaryOperator<Config> fn) {
		return of(fn.apply(config), classLoader, injector);
	}

	public ImmutableServiceContext withClassLoader(UnaryOperator<ClassLoader> fn) {
		return of(config, fn.apply(classLoader), injector);
	}

	public ImmutableServiceContext withInjector(UnaryOperator<Injector> fn) {
		return of(config, classLoader, fn.apply(injector));
	}
}
