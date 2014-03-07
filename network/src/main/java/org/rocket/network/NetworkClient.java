package org.rocket.network;

public interface NetworkClient {
	NetworkCommand write(Object o);
	NetworkCommand close();
	NetworkCommand closeNow();
}
