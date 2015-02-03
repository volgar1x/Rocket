package org.rocket.network.netty;

import com.github.blackrush.acara.EventBus;
import com.github.blackrush.acara.Subscription;
import com.google.common.collect.Sets;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.rocket.Service;
import org.rocket.StartReason;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;
import org.rocket.network.NetworkService;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.SuperviseEvent;
import org.slf4j.Logger;

import javax.inject.Provider;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@ChannelHandler.Sharable // srsly netty????
final class NettyService extends ChannelInboundHandlerAdapter implements NetworkService {

    private final Provider<EventBus> eventBusBuilder;
    private final ControllerFactory controllerFactory;
    private final Provider<EventLoopGroup> eventLoopGroupProvider;
    private final Consumer<ServerBootstrap> configuration;
    private final Consumer<ChannelPipeline> pipelineConfiguration;
    private final Logger logger;

    private Channel chan;
    private EventLoopGroup boss, children;
    private Set<NettyClient> clients;
    private AtomicLong nextId;
    private volatile int maxConnectedClients;

    NettyService(Provider<EventBus> eventBusBuilder, ControllerFactory controllerFactory, Provider<EventLoopGroup> eventLoopGroupProvider, Consumer<ServerBootstrap> configuration, Consumer<ChannelPipeline> pipelineConfiguration, Logger logger) {
        this.eventBusBuilder = eventBusBuilder;
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

        boss = eventLoopGroupProvider.get();
        children = eventLoopGroupProvider.get();
        clients = Sets.newConcurrentHashSet();
        nextId = new AtomicLong(0L);
        maxConnectedClients = 0;

        ServerBootstrap sb = new ServerBootstrap();
        sb.group(boss, children);
        configuration.accept(sb);
        sb.childHandler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel ch) throws Exception {
                pipelineConfiguration.accept(ch.pipeline());
                ch.pipeline().addLast(NettyService.this);
            }
        });

        // NOTE(Blackrush): let it crash
        chan = sb.bind().syncUninterruptibly().channel();

        logger.info("now listening on {}", chan.localAddress());
    }

    @Override
    public void stop() {
        if (chan == null) {
            throw new IllegalStateException();
        }

        logger.debug("stopping");

        // NOTE(Blackrush): "let it crash" doesn't apply when tearing down services
        children.shutdownGracefully().awaitUninterruptibly();
        boss.shutdownGracefully().awaitUninterruptibly();
        chan.close().awaitUninterruptibly();

        // cleans up things a little
        clients.clear();
        clients = null;
        children = null;
        boss = null;
        chan = null;
    }

    @Override
    public int getActualConnectedClients() {
        return clients.size();
    }

    @Override
    public int getMaxConnectedClients() {
        return maxConnectedClients;
    }

    @Override
    public Future<Unit> broadcast(Object msg) {
        clients.forEach(client -> client.channel.writeAndFlush(msg));
        return Futures.unit();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        NettyClient client = new NettyClient(channel, nextId.getAndIncrement(), eventBusBuilder.get());
        channel.attr(RocketNetty.CLIENT_KEY).set(client);

        Set<Object> controllers = controllerFactory.create(client);
        Subscription subscription = client.getEventBus().subscribeMany(controllers);
        channel.attr(RocketNetty.SUBSCRIPTION_KEY).set(subscription);

        clients.add(client);

        if (maxConnectedClients < clients.size()) {
            maxConnectedClients = clients.size();
        }

        client.getEventBus().publish(new ConnectEvent(client, false));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NetworkClient client = ctx.channel().attr(RocketNetty.CLIENT_KEY).getAndRemove();
        Subscription subscription = ctx.channel().attr(RocketNetty.SUBSCRIPTION_KEY).getAndRemove();
        subscription.revoke();
        clients.remove(client);

        client.getEventBus().publish(new ConnectEvent(client, true));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NetworkClient client = ctx.channel().attr(RocketNetty.CLIENT_KEY).get();
        client.getEventBus().publish(new ReceiveEvent(client, msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exception caught", cause);
        NetworkClient client = ctx.channel().attr(RocketNetty.CLIENT_KEY).get();
        client.getEventBus().publish(new SuperviseEvent(client, cause));
    }
}
