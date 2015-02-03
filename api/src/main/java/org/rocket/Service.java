package org.rocket;

public interface Service extends Startable, Stoppable {
    ServicePath path();
	ServicePath dependsOn();
}
