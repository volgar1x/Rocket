package org.rocket;

/**
 * Startable describes an actor that must be stopped.
 */
public interface Stoppable {
    /**
     * Stop the service.
     */
	void stop();
}
