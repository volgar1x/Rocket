package org.rocket.network.event.mbassador;

import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

import java.lang.annotation.Annotation;

public final class HandlerImpl implements Handler {
	private final Filter[] filters;
	private final Invoke delivery;
	private final int priority;
	private final boolean rejectSubtypes;
	private final boolean enabled;
	private final Class<? extends HandlerInvocation> invocation;

	public HandlerImpl(Filter[] filters, Invoke delivery, int priority, boolean rejectSubtypes, boolean enabled, Class<? extends HandlerInvocation> invocation) {
		this.filters = filters;
		this.delivery = delivery;
		this.priority = priority;
		this.rejectSubtypes = rejectSubtypes;
		this.enabled = enabled;
		this.invocation = invocation;
	}

	@Override
	public Filter[] filters() {
		return filters;
	}

	@Override
	public Invoke delivery() {
		return delivery;
	}

	@Override
	public int priority() {
		return priority;
	}

	@Override
	public boolean rejectSubtypes() {
		return rejectSubtypes;
	}

	@Override
	public boolean enabled() {
		return enabled;
	}

	@Override
	public Class<? extends HandlerInvocation> invocation() {
		return invocation;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return Handler.class;
	}
}
