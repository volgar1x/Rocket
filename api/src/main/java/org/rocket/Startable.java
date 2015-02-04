package org.rocket;

/**
 * Startable describes an actor that must be started.
 */
public interface Startable {
    /**
     * Start the service.
     * @param reason why this service is started
     */
	void start(StartReason reason);
}
