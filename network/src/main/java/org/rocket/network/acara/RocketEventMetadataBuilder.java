package org.rocket.network.acara;

import com.github.blackrush.acara.EventMetadata;
import com.github.blackrush.acara.EventMetadataBuilder;
import org.rocket.network.event.ConnectEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.SuperviseEvent;

enum RocketEventMetadataBuilder implements EventMetadataBuilder {
    instance;

    @Override
    public EventMetadata build(Object o) {
        if (o instanceof ConnectEvent) {
            ConnectEvent evt = (ConnectEvent) o;
            return new Events.ConnectEventMetadata(evt.isDisconnecting());
        } else if (o instanceof ReceiveEvent) {
            ReceiveEvent evt = (ReceiveEvent) o;
            return new Events.ComponentWiseEventMetadata<>(evt.getMessage().getClass());
        } else if (o instanceof SuperviseEvent) {
            SuperviseEvent evt = (SuperviseEvent) o;
            return new Events.ComponentWiseEventMetadata<>(evt.getException().getClass());
        }
        return null;
    }
}
