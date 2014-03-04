package org.rocket;

public final class ServiceHandle implements AutoCloseable {
	private final Service service;
	private final ServiceContext ctx;

	private ServiceHandle(Service service, ServiceContext ctx) {
		this.service = service;
		this.ctx = ctx;
	}

	public static ServiceHandle of(Service service, ServiceContext ctx) {
		return new ServiceHandle(service, ctx);
	}

	public void start() {
		service.start(ctx);
	}

	@Override
	public void close() {
		service.stop(ctx);
	}
}
