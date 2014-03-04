package org.rocket;

import com.google.inject.Injector;
import com.typesafe.config.Config;

import static com.google.common.base.Throwables.propagate;

public interface ServiceContext {

	Config getConfig();

	ClassLoader getClassLoader();

	Injector getInjector();

	default Class<?> findClass(String name) {
		try {
			return getClassLoader().loadClass(name);
		} catch (ClassNotFoundException e) {
			throw propagate(e);
		}
	}
}
