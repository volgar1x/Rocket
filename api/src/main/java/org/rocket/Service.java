package org.rocket;

/**
 * A Service is started, stopped, described by a path, and might depend on another service.
 * It typically manages a finite set of resources.
 */
public interface Service extends Startable, Stoppable {
    /**
     * Give the service's path.
     * @return an absolute path
     */
    ServicePath path();

    /**
     * Give the service's parent.
     * @return a path
     */
	ServicePath dependsOn();
}
