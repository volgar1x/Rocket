package org.rocket.network.event.mbassador;

import net.engio.mbassy.PublicationError;
import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.rocket.network.event.NetworkEvent;
import org.rocket.network.event.ReceiveEvent;
import org.rocket.network.event.RecoverEvent;

import java.lang.reflect.Method;

public class RocketHandlerInvocation extends HandlerInvocation {
	public RocketHandlerInvocation(SubscriptionContext context) {
		super(context);
	}

	protected Method getHandler() {
		return getContext().getHandlerMetadata().getHandler();
	}

	public void invoke0(Object listener, Object o) throws Exception {
		Object[] params;

		if (o instanceof ReceiveEvent<?>) {
			ReceiveEvent<?> event = (ReceiveEvent<?>) o;

			params = new Object[] { event.getClient(), event.getMessage() };
		} else if (o instanceof RecoverEvent<?>) {
			RecoverEvent<?> event = (RecoverEvent<?>) o;

			params = new Object[] { event.getClient(), event.getError() };
		} else if (o instanceof NetworkEvent<?>) {
			params = new Object[] { ((NetworkEvent) o).getClient() };
		} else {
			throw new IllegalStateException(String.format("expected a %s but got a %s",
					NetworkEvent.class, o.getClass()));
		}

		getHandler().invoke(listener, params);
	}

	@Override
	public final void invoke(Object listener, Object event) {
		try {
			invoke0(listener, event);
		} catch (Exception e) {
			handlePublicationError(new PublicationError(e, e.getMessage(), getContext().getHandlerMetadata().getHandler(), listener, event));
		}
	}
}
