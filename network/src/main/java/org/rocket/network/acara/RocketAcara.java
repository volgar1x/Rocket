package org.rocket.network.acara;

import com.github.blackrush.acara.EventMetadataBuilder;
import com.github.blackrush.acara.ListenerBuilder;
import org.rocket.network.props.PropValidatorInstantiator;

public final class RocketAcara {
    private RocketAcara() {}

    public static EventMetadataBuilder events() {
        return RocketEventMetadataBuilder.instance;
    }

    public static ListenerBuilder listeners() {
        return RocketListenerBuilder.instance;
    }

    public static ListenerBuilder listeners(PropValidatorInstantiator validationInstantiator) {
        return new RocketListenerBuilder(validationInstantiator);
    }
}
