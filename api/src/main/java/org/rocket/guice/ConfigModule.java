package org.rocket.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.typesafe.config.Config;
import org.rocket.InjectConfig;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.inject.matcher.Matchers.any;
import static org.rocket.guice.GuiceDSL.listener;
import static org.rocket.guice.GuiceDSL.members;

public final class ConfigModule implements Module {
	private final Config config;

	private ConfigModule(Config config) {
		this.config = Objects.requireNonNull(config, "config");
	}

	public static ConfigModule of(Config config) {
		return new ConfigModule(config);
	}

	@Override
	public void configure(Binder binder) {
		binder.bindListener(any(), listener((type, encounter) -> {
			List<Injectee> injectees = Stream.of(type.getRawType().getDeclaredFields())
				.filter(x -> x.isAnnotationPresent(InjectConfig.class))
				.peek(x -> x.setAccessible(true))
				.map(x -> new Injectee(x, x.getAnnotation(InjectConfig.class).value()))
				.collect(Collectors.toList());

			encounter.register(members(instance -> {
				for (Injectee injectee : injectees) {
					try {
						injectee.field.set(instance, config.getAnyRef(injectee.key));
					} catch (IllegalAccessException e) {
						binder.addError(e);
					}
				}
			}));
		}));
	}

	static class Injectee {
		final Field field;
		final String key;

		Injectee(Field field, String key) {
			this.field = Objects.requireNonNull(field, "field");
			this.key = Objects.requireNonNull(key, "key");
		}
	}
}
