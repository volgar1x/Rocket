package org.rocket.network;

import java.util.function.Consumer;

public interface NetworkClient {
	NetworkCommand write(Object o);
	NetworkCommand transaction(Consumer<Transactional> fn);
	NetworkCommand close();
}
