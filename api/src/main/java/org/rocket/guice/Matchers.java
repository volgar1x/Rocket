package org.rocket.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

import java.lang.annotation.Annotation;
import java.util.Objects;

public final class Matchers {
	static class AnnotatedWithMatcher extends AbstractMatcher<TypeLiteral<?>> {
		final Class<? extends Annotation> klass;

		AnnotatedWithMatcher(Class<? extends Annotation> klass) {
			this.klass = Objects.requireNonNull(klass, "klass");
		}

		@Override
		public boolean matches(TypeLiteral<?> t) {
			return t.getRawType().isAnnotationPresent(klass);
		}
	}

	public static Matcher<? super TypeLiteral<?>> annotatedWith(Class<? extends Annotation> klass) {
		return new AnnotatedWithMatcher(klass);
	}
}
