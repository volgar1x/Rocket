package org.rocket.network.props;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.rocket.network.*;

public class PropPresenceValidator implements PropValidator {
    private final PropId pid;
    private final boolean presence;

    public PropPresenceValidator(PropPresence annotation) {
        this.pid = PropIds.type(annotation.value());
        this.presence = annotation.presence();
    }

    @Override
    public Either<Unit, Throwable> validate(NetworkClient client, PropValidated target) {
        return client.getProp(pid).isDefined() != presence
                ? PropValidationException.of("%s must be present: %s", pid, target.describeLocation())
                : Unit.left();
    }
}
