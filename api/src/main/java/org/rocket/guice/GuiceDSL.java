package org.rocket.guice;

import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.util.function.BiConsumer;

public final class GuiceDSL {
	private GuiceDSL() {}

	public static TypeListener listener(BiConsumer<TypeLiteral<?>, TypeEncounter<?>> fn) {
		return new TypeListener() {
			@Override
			public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
				fn.accept(type, encounter);
			}
		};
	}

	public static <T> MembersInjector<T> members(MembersInjector<T> fn) {
		return fn;
	}
}
