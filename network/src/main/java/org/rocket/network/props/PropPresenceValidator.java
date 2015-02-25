package org.rocket.network.props;

import org.rocket.network.MutProp;
import org.rocket.network.NetworkClient;
import org.rocket.network.PropId;
import org.rocket.network.PropValidator;

public class PropPresenceValidator implements PropValidator {
    private final PropId pid;

    public PropPresenceValidator(PropPresence annotation) {
        this.pid = PropIds.type(annotation.value());
    }

    @Override
    public void validate(NetworkClient client) {
        MutProp<Object> prop = client.getProp(pid);

        if (!prop.isDefined()) {
            throw new AssertionError(String.format(
                    "Prop %s must have a value",
                    prop.getId()));
        }
    }
}
