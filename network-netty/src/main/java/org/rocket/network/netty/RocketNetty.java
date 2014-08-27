package org.rocket.network.netty;

import com.github.blackrush.acara.EventBus;
import com.github.blackrush.acara.EventBusBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.AttributeKey;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Promise;
import org.fungsi.concurrent.Promises;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;
import org.rocket.network.NetworkClientService;
import org.rocket.network.NetworkService;
import org.slf4j.Logger;

import javax.inject.Provider;
import java.util.Set;
import java.util.function.Consumer;

public final class RocketNetty {
    private RocketNetty() {}

    public static final Object SUPERVISED_EVENT_NO_INITIAL = new Object();

    public static final AttributeKey<NetworkClient> CLIENT_KEY = AttributeKey.valueOf(RocketNetty.class.getName() + ".CLIENT_KEY");
    public static final AttributeKey<Set<Object>> CONTROLLERS_KEY = AttributeKey.valueOf(RocketNetty.class.getName() + ".CONTROLLERS_KEY");

    public static NetworkService newService(EventBusBuilder eventBus, ControllerFactory controllerFactory, Consumer<ServerBootstrap> configuration, Consumer<ChannelPipeline> pipelineConfiguration, Logger logger) {
        return new NettyService(eventBus, controllerFactory, NioEventLoopGroup::new, configuration, pipelineConfiguration, logger);
    }

    public static NetworkService newService(EventBusBuilder eventBus, ControllerFactory controllerFactory, Provider<EventLoopGroup> eventLoopGroupProvider, Consumer<ServerBootstrap> configuration, Consumer<ChannelPipeline> pipelineConfiguration, Logger logger) {
        return new NettyService(eventBus, controllerFactory, eventLoopGroupProvider, configuration, pipelineConfiguration, logger);
    }

    public static NetworkClientService newClientService(EventBus eventBus, ControllerFactory controllerFactory, Consumer<Bootstrap> configuration, Consumer<ChannelPipeline> pipelineConfiguration, Logger logger) {
        return new NettyClientService(eventBus, controllerFactory, configuration, pipelineConfiguration, logger);
    }

    public static <T> Future<T> toFungsi(io.netty.util.concurrent.Future<T> fut) {
        return toFungsiDownstream(fut);
    }

    public static <T> Future<T> toFungsiDownstream(io.netty.util.concurrent.Future<T> fut) {
        Promise<T> pr = Promises.create();
        fut.addListener(f -> {
            if (f.isSuccess()) {
                pr.complete(fut.getNow());
            } else {
                pr.fail(f.cause());
            }
        });
        return pr;
    }

    public static <T> Promise<T> toFungsiUpstream(io.netty.util.concurrent.Promise<T> pr) {
        Promise<T> res = Promises.create();
        res.onSuccess(pr::setSuccess).onFailure(pr::setFailure);
        return res;
    }
}
