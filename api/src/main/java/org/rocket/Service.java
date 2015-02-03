package org.rocket;

public interface Service extends Startable, Stoppable {
	Class<? extends Service> dependsOn();
}
