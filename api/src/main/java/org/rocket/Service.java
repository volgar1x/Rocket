package org.rocket;

/**
 * A Service is started, stopped, described by a path, and might depend on another service.
 * It typically manages a finite set of resources.
 */
public interface Service extends Startable, Stoppable {
    ServicePath path();
	ServicePath dependsOn();
}
