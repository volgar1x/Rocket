package org.rocket;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Optional;

public final class JUnitMatchers {
	private JUnitMatchers() {}

	static final class OptionalIsEmpty extends BaseMatcher<Optional<?>> {

		@Override
		public boolean matches(Object item) {
			return item instanceof Optional<?> && !((Optional) item).isPresent();
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is empty");
		}
	}

	private static final Matcher<Optional<?>> OPTIONAL_IS_EMPTY = new OptionalIsEmpty();

	public static Matcher<Optional<?>> isEmpty() {
		return OPTIONAL_IS_EMPTY;
	}
}
