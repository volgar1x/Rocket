package org.rocket.network.event.acara;

import org.fungsi.Unit;
import org.fungsi.concurrent.Promise;
import org.fungsi.concurrent.Promises;
import org.rocket.network.NetworkClient;
import org.rocket.network.event.Recover;

class SomeListener {
    final Promise<Unit> recover = Promises.create();

    @Recover
    public void recover(NetworkClient client, Throwable t) {
        recover.set(Unit.left());
    }
}
