package org.rocket.network.props;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.rocket.network.*;

public class PropPresenceValidator implements PropValidator {
    private final PropId pid;
    private final boolean presence;
    private Either<Unit, Throwable> err;

    public PropPresenceValidator(PropPresence annotation) {
        this.pid = PropIds.type(annotation.value());
        this.presence = annotation.presence();
        this.err = PropValidationException.of("Prop %s must have a value", pid);
    }

    @Override
    public Either<Unit, Throwable> validate(NetworkClient client) {
        return client.getProp(pid).isDefined() == presence
                ? Unit.left()
                : err;
    }
}
