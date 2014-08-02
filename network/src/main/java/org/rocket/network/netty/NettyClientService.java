package org.rocket.network.netty;

import com.github.blackrush.acara.EventBus;
import com.github.blackrush.acara.supervisor.event.SupervisedEvent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.rocket.Service;
import org.rocket.ServiceContext;
import org.rocket.network.*;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class NettyClientService extends ChannelInboundHandlerAdapter implements NetworkClientService {
    private final EventBus eventBus;
    private final ControllerFactory controllerFactory;
    private final Consumer<Bootstrap> configuration;
    private final Consumer<ChannelPipeline> pipelineConfiguration;
    private final Logger logger;

    Channel chan;
    EventLoopGroup worker;
    Set<Object> controllers;
    HashPropBag props;

    NettyClientService(EventBus eventBus, ControllerFactory controllerFactory, Consumer<Bootstrap> configuration, Consumer<ChannelPipeline> pipelineConfiguration, Logger logger) {
        this.eventBus = eventBus;
        this.controllerFactory = controllerFactory;
        this.configuration = configuration;
        this.pipelineConfiguration = pipelineConfiguration;
        this.logger = logger;
    }

    @Override
    public Optional<Class<? extends Service>> dependsOn() {
        return Optional.empty();
    }

    @Override
    public void start(ServiceContext ctx) {
        if (chan != null) {
            throw new IllegalStateException();
        }

        logger.debug("starting");

        props = new HashPropBag();
        worker = new NioEventLoopGroup();
        controllers = controllerFactory.create(this);
        eventBus.subscribeMany(controllers);

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
        logger.debug("connected to {}", chan.remoteAddress());
    }

    @Override
    public void stop(ServiceContext ctx) {
        if (chan == null) {
            throw new IllegalStateException();
        }

        logger.debug("stopping");

        chan.close().awaitUninterruptibly();
        worker.shutdownGracefully().awaitUninterruptibly();
        eventBus.unsubscribeMany(controllers);

        chan = null;
        worker = null;
        controllers = null;
        props = null;
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
        Tx tx = new Tx();
        fn.accept(tx);
        tx.forEach(chan::write);
        chan.flush();
        return Futures.unit();
    }

    @Override
    public Future<Unit> close() {
        return RocketNetty.toFungsi(chan.close()).toUnit();
    }

    class Tx extends LinkedList<Object> implements NetworkTransaction {
        @Override
        public void write(Object msg) {
            add(msg);
        }
    }

    @Override
    public <T> Prop<T> getProp(PropKey key) {
        return props.getProp(key);
    }

    @Override
    public <T> MutProp<T> getMutProp(PropKey key) {
        return props.getMutProp(key);
    }

    @Override
    public Stream<PropKey> getPresentPropKeys() {
        return props.getPresentPropKeys();
    }

    @Override
    public int getNrPresentProps() {
        return props.getNrPresentProps();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        eventBus.publishAsync(new ConnectEvent(this, false));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        eventBus.publishAsync(new ConnectEvent(this, true));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        eventBus.publishAsync(new ReceiveEvent(this, msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        eventBus.publishAsync(new SupervisedEvent(RocketNetty.SUPERVISED_EVENT_NO_INITIAL, cause));
    }
}
