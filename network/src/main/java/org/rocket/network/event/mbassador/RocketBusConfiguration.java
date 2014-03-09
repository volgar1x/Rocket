package org.rocket.network.event.mbassador;

import net.engio.mbassy.bus.config.BusConfiguration;

public final class RocketBusConfiguration {
	private RocketBusConfiguration() {}

	public static BusConfiguration Default() {
		BusConfiguration config = BusConfiguration.Default();

		config.setMetadataReader(new RocketMetadataReader());

		return config;
	}
}
