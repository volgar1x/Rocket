package org.rocket;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Throwables.propagate;

public interface Try<T> {
	T getOrThrow();

	T orElse(Function<Throwable, ? extends T> fn);

	<A> Try<A> flatMap(Function<T, Try<A>> fn);

	<A> Try<A> map(Function<T, A> fn);

	Stream<T> stream();
	Optional<T> optional();

	public static <T> Try<T> of(Callable<T> fn) {
		try {
			return success(fn.call());
		} catch (Exception e) {
			return failure(e);
		}
	}

	public static <T> Try<T> success(T value) {
		return new Success<>(value);
	}

	@SuppressWarnings("unchecked")
	public static <T> Try<T> failure(Throwable cause) {
		return (Try<T>) new Failure(cause);
	}

	static class Failure implements Try<Object> {

		private final Throwable cause;

		Failure(Throwable cause) {
			this.cause = Objects.requireNonNull(cause, "cause");
		}

		@Override
		public Object getOrThrow() {
			throw propagate(cause);
		}

		@Override
		public Object orElse(Function<Throwable, ?> fn) {
			return fn.apply(cause);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <A> Try<A> flatMap(Function<Object, Try<A>> fn) {
			return (Try<A>) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <A> Try<A> map(Function<Object, A> fn) {
			return (Try<A>) this;
		}

		@Override
		public Stream<Object> stream() {
			return Stream.empty();
		}

		@Override
		public Optional<Object> optional() {
			return Optional.empty();
		}
	}

	static class Success<T> implements Try<T> {
		private final T value;

		Success(T value) {
			this.value = Objects.requireNonNull(value, "value");
		}

		@Override
		public T getOrThrow() {
			return value;
		}

		@Override
		public T orElse(Function<Throwable, ? extends T> fn) {
			return value;
		}

		@Override
		public <A> Try<A> flatMap(Function<T, Try<A>> fn) {
			return fn.apply(value);
		}

		@Override
		public <A> Try<A> map(Function<T, A> fn) {
			return new Success<>(fn.apply(value));
		}

		@Override
		public Stream<T> stream() {
			return Stream.of(value);
		}

		@Override
		public Optional<T> optional() {
			return Optional.of(value);
		}
	}
}
