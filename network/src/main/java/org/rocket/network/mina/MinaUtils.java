package org.rocket.network.mina;

import org.apache.mina.core.future.*;
import org.fungsi.Either;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Promise;
import org.fungsi.concurrent.Promises;

import java.util.Optional;

public final class MinaUtils {
    private MinaUtils() { }

    public static Future<Unit> toFungsi(IoFuture fut) {
        Promise<Unit> p = Promises.create();

        fut.addListener(f -> p.set(findFailure(f) // find failure
                .<Either<Unit, Throwable>>map(Either::right) // places it to right if there is really a failure
                .orElse(Unit.left()))); // give a Unit.left() if there isn't

        return p;
    }

    public static Optional<Throwable> findFailure(IoFuture fut) {
        if (fut instanceof WriteFuture) {
            return Optional.ofNullable(((WriteFuture) fut).getException());
        } else if (fut instanceof ReadFuture) {
            return Optional.ofNullable(((ReadFuture) fut).getException());
        } else if (fut instanceof ConnectFuture) {
            return Optional.ofNullable(((ConnectFuture) fut).getException());
        } else if (fut instanceof CloseFuture) {
            return Optional.empty();
        }
        throw new Error("unknown future " + fut.getClass());
    }
}
