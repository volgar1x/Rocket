package org.rocket.network;

import com.google.common.collect.ImmutableList;
import org.fungsi.Either;
import org.fungsi.Unit;

public interface PropValidator {
    Either<Unit, Throwable> validate(NetworkClient client);

    default void hardValidate(NetworkClient client) {
        Either.unsafe(validate(client));
    }

    default boolean softValidate(NetworkClient client) {
        return validate(client).isLeft();
    }

    public static PropValidator aggregate(ImmutableList<PropValidator> validators) {
        return client -> {
            for (PropValidator validator : validators) {
                Either<Unit, Throwable> res = validator.validate(client);
                if (res.isRight()) {
                    return res;
                }
            }
            return Unit.left();
        };
    }
}
