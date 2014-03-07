package org.rocket.network;

import java.time.Duration;

public interface NetworkCommand {
	void now();
	void now(Duration max);

	void async();
	void async(Duration max);
}
