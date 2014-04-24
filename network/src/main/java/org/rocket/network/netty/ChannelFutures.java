package org.rocket.network.netty;

import io.netty.channel.ChannelFuture;
import org.fungsi.Either;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Promise;
import org.fungsi.concurrent.Promises;

public final class ChannelFutures {
    private ChannelFutures() {}

    public static Future<Unit> toFungsi(ChannelFuture fut) {
        Promise<Unit> p = Promises.create();

        fut.addListener(f -> {
            if (f.isSuccess()) {
                p.set(Unit.left());
            } else {
                p.set(Either.failure(f.cause()));
            }
        });

        return p;
    }
}
