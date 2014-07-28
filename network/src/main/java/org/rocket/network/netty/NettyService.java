package org.rocket.network.netty;

import com.github.blackrush.acara.EventBus;
import com.github.blackrush.acara.supervisor.event.SupervisedEvent;
import com.google.common.collect.Sets;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.fungsi.concurrent.Futures;
import org.rocket.Service;
import org.rocket.ServiceContext;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;
import org.rocket.network.NetworkService;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.ReceiveEvent;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class NettyService extends ChannelInboundHandlerAdapter implements NetworkService {

    private final Supplier<EventBus> eventBusFactory;
    private final ControllerFactory controllerFactory;
    private final Consumer<ServerBootstrap> configuration;

    private Channel chan;
    private EventLoopGroup loop;
    private Set<NettyClient> clients;
    private long nextId;
    private volatile int maxConnectedClients;

    NettyService(Supplier<EventBus> eventBusFactory, ControllerFactory controllerFactory, Consumer<ServerBootstrap> configuration) {
        this.eventBusFactory = eventBusFactory;
        this.controllerFactory = controllerFactory;
        this.configuration = configuration;
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

        clients = Sets.newConcurrentHashSet();
        nextId = 0;
        maxConnectedClients = 0;

        ServerBootstrap sb = new ServerBootstrap();
        sb.group(loop = new NioEventLoopGroup());
        sb.childHandler(this);
        configuration.accept(sb);

        // NOTE(Blackrush): let it crash
        chan = sb.bind().syncUninterruptibly().channel();
    }

    @Override
    public void stop(ServiceContext ctx) {
        if (chan == null) {
            throw new IllegalStateException();
        }

        // NOTE(Blackrush): "let it crash" doesn't apply when tearing down services
        loop.shutdownGracefully().awaitUninterruptibly();
        chan.close().awaitUninterruptibly();

        // cleans up things a little
        clients.clear();
        clients = null;
        loop = null;
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
        return clients.stream()
                .map(x -> x.write(msg))
                .collect(Futures.collect())
                .toUnit()
                ;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyClient client = new NettyClient(ctx.channel(), nextId++, eventBusFactory.get());
        ctx.channel().attr(Netty.CLIENT_KEY).set(client);

        Set<Object> controllers = controllerFactory.create(client);
        ctx.channel().attr(Netty.CONTROLLERS_KEY).set(controllers);

        clients.add(client);
        client.getEventBus().subscribeMany(controllers);

        if (maxConnectedClients < clients.size()) {
            maxConnectedClients = clients.size();
        }

        client.getEventBus().publishAsync(new ConnectEvent(client, false));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NetworkClient client = ctx.channel().attr(Netty.CLIENT_KEY).get();
        Set<Object> controllers = ctx.channel().attr(Netty.CONTROLLERS_KEY).get();

        client.getEventBus().publishAsync(new ConnectEvent(client, true));

        client.getEventBus().unsubscribeMany(controllers);
        clients.remove(client);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NetworkClient client = ctx.channel().attr(Netty.CLIENT_KEY).get();
        client.getEventBus().publishAsync(new ReceiveEvent(client, msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().attr(Netty.CLIENT_KEY).get()
                .getEventBus().publishAsync(new SupervisedEvent(Netty.SUPERVISED_EVENT_NO_INITIAL, cause));
    }
}
