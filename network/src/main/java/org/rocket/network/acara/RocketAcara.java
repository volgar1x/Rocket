package org.rocket.network.acara;

import com.github.blackrush.acara.EventMetadataBuilder;
import com.github.blackrush.acara.ListenerBuilder;

public final class RocketAcara {
    private RocketAcara() {}

    public static EventMetadataBuilder events() {
        return RocketEventMetadataBuilder.instance;
    }

    public static ListenerBuilder listeners() {
        return RocketListenerBuilder.instance;
    }
}
