package org.rocket.network.netty;

import com.github.blackrush.acara.EventBus;
import com.github.blackrush.acara.Subscription;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.rocket.Service;
import org.rocket.StartReason;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClientService;
import org.rocket.network.NetworkTransaction;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.SuperviseEvent;
import org.slf4j.Logger;

import javax.inject.Provider;
import java.util.function.Consumer;

final class NettyClientService extends ChannelInboundHandlerAdapter implements NetworkClientService {
    private final EventBus eventBus;
    private final ControllerFactory controllerFactory;
    private final Provider<EventLoopGroup> eventLoopGroupProvider;
    private final Consumer<Bootstrap> configuration;
    private final Consumer<ChannelPipeline> pipelineConfiguration;
    private final Logger logger;

    Channel chan;
    EventLoopGroup worker;
    Subscription subscription;

    NettyClientService(EventBus eventBus, ControllerFactory controllerFactory, Provider<EventLoopGroup> eventLoopGroupProvider, Consumer<Bootstrap> configuration, Consumer<ChannelPipeline> pipelineConfiguration, Logger logger) {
        this.eventBus = eventBus;
        this.controllerFactory = controllerFactory;
        this.eventLoopGroupProvider = eventLoopGroupProvider;
        this.configuration = configuration;
        this.pipelineConfiguration = pipelineConfiguration;
        this.logger = logger;
    }

    @Override
    public Class<? extends Service> dependsOn() {
        return null;
    }

    @Override
    public void start(StartReason reason) {
        if (chan != null) {
            throw new IllegalStateException();
        }

        logger.debug("starting");

        worker = eventLoopGroupProvider.get();
        subscription = eventBus.subscribeMany(controllerFactory.create(this));

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker);
        configuration.accept(bootstrap);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                pipelineConfiguration.accept(ch.pipeline());
                ch.pipeline().addLast(NettyClientService.this);
            }
        });

        chan = bootstrap.connect().syncUninterruptibly().channel();
        logger.info("connected to {}", chan.remoteAddress());
    }

    @Override
    public void stop() {
        if (chan == null) {
            throw new IllegalStateException();
        }

        logger.debug("stopping");

        chan.close().awaitUninterruptibly();
        worker.shutdownGracefully().awaitUninterruptibly();
        subscription.revoke();

        chan = null;
        worker = null;
        subscription = null;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public Future<Unit> write(Object msg) {
        chan.writeAndFlush(msg);
        return Futures.unit();
    }

    @Override
    public Future<Unit> transaction(Consumer<NetworkTransaction> fn) {
        fn.accept(chan::write);
        chan.flush();
        return Futures.unit();
    }

    @Override
    public Future<Unit> close() {
        return RocketNetty.toFungsi(chan.close()).toUnit();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        eventBus.publish(new ConnectEvent(this, false));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        eventBus.publish(new ConnectEvent(this, true));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        eventBus.publish(new ReceiveEvent(this, msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        eventBus.publish(new SuperviseEvent(this, cause));
    }
}
