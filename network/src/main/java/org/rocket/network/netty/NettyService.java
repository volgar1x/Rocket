package org.rocket.network.netty;

import com.google.common.collect.ImmutableSet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import net.engio.mbassy.IPublicationErrorHandler;
import net.engio.mbassy.PublicationError;
import net.engio.mbassy.bus.IMessageBus;
import org.rocket.Service;
import org.rocket.ServiceContext;
import org.rocket.network.NetworkCommand;
import org.rocket.network.NetworkService;
import org.rocket.network.event.*;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class NettyService<C extends NettyClient> implements NetworkService<C>, IPublicationErrorHandler {

	private final ServerBootstrap bootstrap;
	private final EventLoopGroup boss, worker;
	private final Function<Channel, C> clientFactory;
	private final IMessageBus<NetworkEvent<C>, ?> eventBus;
	private final Logger logger;
	private final Set<C> clients = new HashSet<>();

	private Optional<Channel> server;
	private int maxConnectedClients;

	public NettyService(Function<Channel, C> clientFactory, IMessageBus<NetworkEvent<C>, ?> eventBus, Consumer<Channel> initializer, SocketAddress localAddr, Logger logger) {
		this.clientFactory = clientFactory;
		this.eventBus = eventBus;
		this.logger = logger;
		this.eventBus.addErrorHandler(this);

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
		return new BroadcastCommand(worker.next(), clients, o);
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
	public IMessageBus<NetworkEvent<C>, ?> getEventBus() {
		return eventBus;
	}

	@Override
	public void handleError(PublicationError error) {
		Throwable cause = error.getCause();

		if (cause instanceof Error) {
			throw (Error) cause; // an error should not be catched
		}

		@SuppressWarnings("unchecked")
		C client = ((NetworkEvent<C>) error.getPublishedObject()).getClient();

		eventBus.post(new RecoverEvent<>(client, cause)).now();
		logger.error("unhandled exception", cause);
	}

	final AttributeKey<C> ATTR = AttributeKey.valueOf(NettyService.class.getName() + "$Clients.ATTR." + this.hashCode());

	class Clients extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			C client = clientFactory.apply(ctx.channel());
			clients.add(client);
			ctx.channel().attr(ATTR).set(client);
			int connected = getActualConnectedClients();
			if (maxConnectedClients < connected) {
				maxConnectedClients = connected;
			}

			ctx.fireChannelActive();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			C client = ctx.channel().attr(ATTR).getAndRemove();
			clients.remove(client);

			ctx.fireChannelInactive();
		}
	}

	class Dispatch extends ChannelInboundHandlerAdapter {
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			C client = ctx.channel().attr(ATTR).get();
			eventBus.post(new ConnectEvent<>(client)).now();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			C client = ctx.channel().attr(ATTR).get();
			eventBus.post(new DisconnectEvent<>(client)).now();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			C client = ctx.channel().attr(ATTR).get();
			eventBus.post(new ReceiveEvent<>(client, msg)).now();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			C client = ctx.channel().attr(ATTR).get();
			eventBus.post(new RecoverEvent<>(client, cause)).now();
		}
	}
}
