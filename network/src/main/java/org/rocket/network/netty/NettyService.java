package org.rocket.network.netty;

import com.google.common.collect.ImmutableSet;
import org.rocket.Service;
import org.rocket.ServiceContext;
import org.rocket.network.NetworkCommand;
import org.rocket.network.NetworkService;

import java.util.Optional;

public class NettyService<C extends NettyClient> implements NetworkService<C> {
	@Override
	public int getActualConnectedClients() {
		return 0;
	}

	@Override
	public int getMaxConnectedClients() {
		return 0;
	}

	@Override
	public ImmutableSet<C> getClients() {
		return null;
	}

	@Override
	public NetworkCommand broadcast(Object o) {
		return null;
	}

	@Override
	public Optional<Class<? extends Service>> dependsOn() {
		return null;
	}

	@Override
	public void start(ServiceContext ctx) {

	}

	@Override
	public void stop(ServiceContext ctx) {

	}
}
