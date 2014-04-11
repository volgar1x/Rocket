package org.rocket.network;

import org.fungsi.Unit;
import org.fungsi.concurrent.Future;

import java.time.Duration;

public interface NetworkCommand {
	void now();
	void now(Duration max);

	Future<Unit> async();
	Future<Unit> async(Duration max);

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
            public Future<Unit> async() {
                return self.async().bind(it -> other.async());
            }

            @Override
            public Future<Unit> async(Duration max) {
                return self.async(max).bind(it -> other.async(max));
            }
        };
    }
}
