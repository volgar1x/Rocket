package org.rocket.network;

import java.time.Duration;

public interface NetworkCommand {
	void now();
	void now(Duration max);

	void async();
	void async(Duration max);

    default NetworkCommand then(NetworkCommand other) {
        NetworkCommand self = this;
        return new NetworkCommand() {
            @Override
            public void now() {
                self.now();
                other.now();
            }

            @Override
            public void now(Duration max) {
                self.now(max);
                other.now(max);
            }

            @Override
            public void async() {
                self.async();
                other.async();
            }

            @Override
            public void async(Duration max) {
                self.async(max);
                other.async(max);
            }
        };
    }
}
