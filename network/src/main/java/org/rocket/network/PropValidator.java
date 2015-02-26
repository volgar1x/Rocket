package org.rocket.network;

import com.google.common.collect.ImmutableList;
import org.fungsi.Either;
import org.fungsi.Unit;

public interface PropValidator {
    Either<Unit, Throwable> validate(NetworkClient client, PropValidated target);

    default void hardValidate(NetworkClient client, PropValidated target) {
        Either.unsafe(validate(client, target));
    }

    default boolean softValidate(NetworkClient client, PropValidated target) {
        return validate(client, target).isLeft();
    }

    public static PropValidator aggregate(ImmutableList<PropValidator> validators) {
        return (client, target) -> {
            for (PropValidator validator : validators) {
                Either<Unit, Throwable> res = validator.validate(client, target);
                if (res.isRight()) {
                    return res;
                }
            }
            return Unit.left();
        };
    }
}
