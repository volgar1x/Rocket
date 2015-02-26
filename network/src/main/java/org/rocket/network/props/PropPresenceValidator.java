package org.rocket.network.props;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.rocket.network.*;

public class PropPresenceValidator implements PropValidator {
    private final PropId pid;
    private final boolean presence;
    private final String format;

    public PropPresenceValidator(PropPresence annotation) {
        this.pid = PropIds.type(annotation.value());
        this.presence = annotation.presence();
        this.format = annotation.message().equals(PropPresence.NIL)
                ? "%s must be present: %s"
                : annotation.message();
    }

    @Override
    public Either<Unit, Throwable> validate(NetworkClient client, PropValidated target) {
        return client.getProp(pid).isDefined() != presence
                ? PropValidationException.of(format, pid, target.describeLocation())
                : Unit.left();
    }
}
