package org.rocket.network.netty;

import com.google.common.collect.ImmutableSet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import net.engio.mbassy.bus.IMessageBus;
import org.rocket.Service;
import org.rocket.ServiceContext;
import org.rocket.network.NetworkCommand;
import org.rocket.network.NetworkService;
import org.rocket.network.event.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NettyService<C extends NettyClient> implements NetworkService<C> {

	private final ServerBootstrap bootstrap;
	private final EventLoopGroup boss, worker;
	private final Function<Channel, C> clientFactory;
	private final IMessageBus<NetworkEvent<C>, ?> eventBus;
	private final Set<C> clients = new HashSet<>();

	private Optional<Channel> server;
	private int maxConnectedClients;

	public NettyService(Function<Channel, C> clientFactory, IMessageBus<NetworkEvent<C>, ?> eventBus, Consumer<Channel> initializer) {
		this.clientFactory = clientFactory;
		this.eventBus = eventBus;

		this.boss   = new NioEventLoopGroup();
		this.worker = new NioEventLoopGroup();

		this.bootstrap = new ServerBootstrap()
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
		server = Optional.of(bootstrap.bind().awaitUninterruptibly().channel());
	}

	@Override
	public void stop(ServiceContext ctx) {
		server.get().close()       .awaitUninterruptibly();
		worker.shutdownGracefully().awaitUninterruptibly();
		boss.shutdownGracefully()  .awaitUninterruptibly();
	}

	@Override
	public NetworkCommand broadcast(Object o) {
		return new BroadcastCommand(o);
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

	final AttributeKey<C> ATTR = AttributeKey.valueOf(Clients.class.getName() + ".ATTR");

	class Clients extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			C client = clientFactory.apply(ctx.channel());
			clients.add(client);
			ctx.attr(ATTR).set(client);

			int connected = getActualConnectedClients();
			if (maxConnectedClients < connected) {
				maxConnectedClients = connected;
			}

			ctx.fireChannelActive();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			C client = ctx.attr(ATTR).getAndRemove();
			clients.remove(client);

			ctx.fireChannelInactive();
		}
	}

	class Dispatch extends ChannelInboundHandlerAdapter {
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			C client = ctx.attr(ATTR).get();
			eventBus.post(new ConnectEvent<>(client)).now();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			C client = ctx.attr(ATTR).get();
			eventBus.post(new DisconnectEvent<>(client)).now();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			C client = ctx.attr(ATTR).get();
			eventBus.post(new ReceiveEvent<>(client, msg)).now();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			C client = ctx.attr(ATTR).get();
			eventBus.post(new RecoverEvent<>(client, cause)).now();
		}
	}

	class BroadcastCommand implements NetworkCommand {
		private final Object o;

		BroadcastCommand(Object o) {
			this.o = o;
		}

		@Override
		public void now() {
			collect(clients.stream().map(x -> x.channel.writeAndFlush(o))).awaitUninterruptibly();
		}

		@Override
		public void now(Duration max) {
			collect(clients.stream().map(x -> x.channel.writeAndFlush(o))).awaitUninterruptibly(max.toMillis());
		}

		@Override
		public void async() {
			for (C client : clients) {
				client.channel.writeAndFlush(o);
			}
		}

		@Override
		public void async(Duration max) {
			async();
		}
	}

	ChannelFuture collect(Stream<ChannelFuture> s) {
		return collect(s.collect(Collectors.toList()));
	}

	ChannelFuture collect(List<ChannelFuture> futures) {
		ChannelPromise res = server.get().newPromise();

		AtomicInteger len = new AtomicInteger(futures.size());

		for (ChannelFuture fut : futures) {
			fut.addListener(f -> {
				if (res.isDone()) return;

				if (f.isSuccess() && len.decrementAndGet() <= 0) {
					res.setSuccess();
				} else {
					res.setFailure(f.cause());
				}
			});
		}

		return res;
	}
}
