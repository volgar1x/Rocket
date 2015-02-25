package org.rocket.network;

import org.fungsi.Either;
import org.fungsi.Unit;

public final class PropValidationException extends RuntimeException {
    public PropValidationException() {
    }

    public PropValidationException(String message) {
        super(message);
    }

    public PropValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropValidationException(Throwable cause) {
        super(cause);
    }

    public PropValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    public static Either<Unit, Throwable> of(String message, Object... args) {
        return Either.right(new PropValidationException(String.format(message, args)));
    }
}
