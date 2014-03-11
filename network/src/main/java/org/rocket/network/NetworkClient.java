package org.rocket.network;

import java.net.SocketAddress;
import java.util.function.Consumer;

public interface NetworkClient {
    SocketAddress getLocalAddress();
    SocketAddress getRemoteAddress();

	NetworkCommand write(Object o);
	NetworkCommand transaction(Consumer<Transactional> fn);
	NetworkCommand close();
}
