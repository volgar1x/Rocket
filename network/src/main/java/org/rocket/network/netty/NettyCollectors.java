package org.rocket.network.netty;

import com.google.common.collect.ImmutableSet;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class NettyCollectors {
	private NettyCollectors() {}

	private static final class Group implements Collector<Channel, ChannelGroup, ChannelGroupFuture> {
		private final EventExecutor worker;
		private final Function<ChannelGroup, ChannelGroupFuture> finisher;

		private Group(EventExecutor worker, Function<ChannelGroup, ChannelGroupFuture> finisher) {
			this.worker = worker;
			this.finisher = finisher;
		}

		@Override
		public Supplier<ChannelGroup> supplier() {
			return () -> new DefaultChannelGroup(worker);
		}

		@Override
		public BiConsumer<ChannelGroup, Channel> accumulator() {
			return ChannelGroup::add;
		}

		@Override
		public BinaryOperator<ChannelGroup> combiner() {
			return (a, b) -> {
				a.addAll(b);
				return a;
			};
		}

		@Override
		public Function<ChannelGroup, ChannelGroupFuture> finisher() {
			return finisher;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return ImmutableSet.of();
		}
	}

	public static Collector<Channel, ?, ? extends Future<Void>> collect(EventExecutor worker, Function<ChannelGroup, ChannelGroupFuture> fn) {
		return new Group(worker, fn);
	}
}
