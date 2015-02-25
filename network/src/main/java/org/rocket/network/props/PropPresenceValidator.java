package org.rocket.network.props;

import org.rocket.network.Prop;
import org.rocket.network.PropValidator;

public class PropPresenceValidator implements PropValidator<Object> {
    @Override
    public void validate(Prop<Object> prop) {
        if (!prop.isDefined()) {
            throw new AssertionError(String.format(
                    "Prop %s must have a value",
                    prop.getId()));
        }
    }
}
