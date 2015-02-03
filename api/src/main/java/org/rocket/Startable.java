package org.rocket;

/**
 * Startable describes an actor that must be started.
 */
public interface Startable {
	void start(StartReason reason);
}
