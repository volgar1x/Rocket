package org.rocket.network.netty;

import io.netty.channel.ChannelFuture;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Promise;
import org.fungsi.concurrent.Promises;

public final class ChannelFutures {
    private ChannelFutures() {}

    public static Future<Unit> toFungsi(ChannelFuture fut) {
        Promise<Unit> p = Promises.create();
        fut.addListener(it -> p.set(Unit.left()));
        return p;
    }
}
