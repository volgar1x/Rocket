package org.rocket.network;

import com.google.common.collect.ImmutableList;

public interface PropValidator {
    void validate(NetworkClient client);

    public static PropValidator aggregate(ImmutableList<PropValidator> validators) {
        return new PropValidator() {
            @Override
            public void validate(NetworkClient client) {
                for (PropValidator validator : validators) {
                    validator.validate(client);
                }
            }
        };
    }
}
