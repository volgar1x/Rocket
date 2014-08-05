package org.rocket.network.acara;

import com.github.blackrush.acara.dispatch.Dispatcher;
import org.fungsi.Either;
import org.rocket.network.NetworkClient;
import org.rocket.network.event.NetworkEvent;

import java.util.List;

final class RocketPropValidatorDispatcher implements Dispatcher {
    final Dispatcher dispatcher;
    final List<Validations.Validation> validations;

    public RocketPropValidatorDispatcher(Dispatcher dispatcher, List<Validations.Validation> validations) {
        this.dispatcher = dispatcher;
        this.validations = validations;
    }

    @Override
    public Either<Object, Throwable> dispatch(Object listener, Object o) {
        NetworkEvent event = (NetworkEvent) o;
        NetworkClient client = event.getClient();
        for (Validations.Validation validation : validations) {
            if (!validation.validate(client)) {
                throw new IllegalStateException(validation.describe());
            }
        }

        return dispatcher.dispatch(listener, event);
    }
}
