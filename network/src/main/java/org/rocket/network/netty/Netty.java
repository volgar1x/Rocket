package org.rocket.network.netty;

import com.github.blackrush.acara.EventBus;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.util.AttributeKey;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Promise;
import org.fungsi.concurrent.Promises;
import org.rocket.network.NetworkClient;
import org.rocket.network.NetworkService;

import java.util.function.Consumer;

public final class Netty {
    private Netty() {}

    public static final Object SUPERVISED_EVENT_NO_INITIAL = new Object();

    public static final AttributeKey<NetworkClient> CLIENT_KEY = AttributeKey.valueOf(Netty.class.getName() + ".CLIENT_KEY");

    public static NetworkService newService(EventBus eventBus, Consumer<ServerBootstrap> configuration) {
        return new NettyService(eventBus, configuration);
    }

    public static <T> Future<T> toFungsi(io.netty.util.concurrent.Future<T> fut) {
        return toFungsiDownstream(fut);
    }

    public static <T> Future<T> toFungsiDownstream(io.netty.util.concurrent.Future<T> fut) {
        Promise<T> pr = Promises.create();
        fut.addListener(f -> pr.complete(fut.getNow()));
        return pr;
    }

    public static <T> Promise<T> toFungsiUpstream(io.netty.util.concurrent.Promise<T> pr) {
        Promise<T> res = Promises.create();
        res.onSuccess(pr::setSuccess).onFailure(pr::setFailure);
        return res;
    }
}
