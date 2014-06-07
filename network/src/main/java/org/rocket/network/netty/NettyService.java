package org.rocket.network.netty;

import com.github.blackrush.acara.EventBus;
import com.google.common.collect.ImmutableSet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;
import org.fungsi.concurrent.Timer;
import org.fungsi.concurrent.Timers;
import org.rocket.Service;
import org.rocket.ServiceContext;
import org.rocket.network.NetworkCommand;
import org.rocket.network.NetworkService;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.DisconnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.RecoverEvent;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class NettyService<C extends NettyClient> implements NetworkService<C> {
    public static final AttributeKey<Object> CLIENT_ATTR = AttributeKey.valueOf(NettyService.class.getName() + ".CLIENT_ATTR");

    @SuppressWarnings("unchecked")
    public static <C> Attribute<C> clientAttribute(AttributeMap map) {
        return (Attribute) map.attr(CLIENT_ATTR);
    }

	private final ServerBootstrap bootstrap;
	private final EventLoopGroup boss, worker;
	private final BiFunction<Channel, NettyService<C>, C> clientFactory;
	private final EventBus eventBus;
    private final ScheduledExecutorService scheduler;
	private final Logger logger;
	private final Set<C> clients = new HashSet<>();

	private Optional<Channel> server;
	private int maxConnectedClients;

	public NettyService(BiFunction<Channel, NettyService<C>, C> clientFactory, EventBus eventBus, Consumer<Channel> initializer, SocketAddress localAddr, ScheduledExecutorService scheduler, Logger logger) {
		this.clientFactory = clientFactory;
		this.eventBus = eventBus;
        this.scheduler = scheduler;
        this.logger = logger;

		this.boss   = new NioEventLoopGroup();
		this.worker = new NioEventLoopGroup();

		this.bootstrap = new ServerBootstrap()
			.localAddress(localAddr)
			.group(boss, worker)
			.channelFactory(NioServerSocketChannel::new)
			.childHandler(new ChannelInitializer<Channel>() {
				protected void initChannel(Channel ch) throws Exception {
					ch.pipeline().addLast(Clients.class.getName(), new Clients());
					initializer.accept(ch);
					ch.pipeline().addLast(Dispatch.class.getName(), new Dispatch());
				}
			})
			;
	}

	@Override
	public Optional<Class<? extends Service>> dependsOn() {
		return Optional.empty();
	}

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public Timer newTimer() {
        return Timers.wrap(getScheduler());
    }

	@Override
	public void start(ServiceContext ctx) {
		logger.debug("starting...");
		server = Optional.of(bootstrap.bind().awaitUninterruptibly().channel());
		logger.info("started");
	}

	@Override
	public void stop(ServiceContext ctx) {
		logger.debug("stopping...");
		server.get().close()       .awaitUninterruptibly();
		worker.shutdownGracefully().awaitUninterruptibly();
		boss.shutdownGracefully()  .awaitUninterruptibly();
		logger.info("stopped");
	}

	@Override
	public NetworkCommand broadcast(Stream<C> clients, Object o) {
		return new BroadcastCommand(clients.map(it -> it.channel), o, this::newTimer);
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
	public ImmutableSet<C> getClients() {
		return ImmutableSet.copyOf(clients);
	}

	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	class Clients extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			C client = clientFactory.apply(ctx.channel(), NettyService.this);
			clients.add(client);
			ctx.channel().attr(CLIENT_ATTR).set(client);
			int connected = getActualConnectedClients();
			if (maxConnectedClients < connected) {
				maxConnectedClients = connected;
			}

			ctx.fireChannelActive();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelInactive();

            C client = NettyService.<C>clientAttribute(ctx.channel()).getAndRemove();
            clients.remove(client);
		}

    }

	class Dispatch extends ChannelInboundHandlerAdapter {

        @Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			C client = NettyService.<C>clientAttribute(ctx.channel()).get();
			eventBus.publishSync(new ConnectEvent<>(client));
		}
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			C client = NettyService.<C>clientAttribute(ctx.channel()).get();
			eventBus.publishSync(new DisconnectEvent<>(client));
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			C client = NettyService.<C>clientAttribute(ctx.channel()).get();
			eventBus.publishSync(new ReceiveEvent<>(client, msg));
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			C client = NettyService.<C>clientAttribute(ctx.channel()).get();
			eventBus.publishSync(new RecoverEvent<>(client, cause));
		}

    }
}
