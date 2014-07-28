package org.rocket.network;

import java.util.Set;

@FunctionalInterface
public interface ControllerFactory {
    Set<Object> create(NetworkClient client);
}
