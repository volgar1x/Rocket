package org.rocket;

import java.util.function.BiConsumer;

public interface ServiceGraph {
    /**
     * Top-down traversal of the graph.
     * @param fn non-null consumer
     * @see Service#start(StartReason)
     */
    void sink(BiConsumer<@Nullable Service, Service> fn);

    /**
     * Bottom-up traversal of the graph.
     * @param fn non-null consumer
     * @see Service#stop()
     */
    void emerge(BiConsumer<@Nullable Service, Service> fn);

    /**
     * Return the root of the graph.
     * @return non-null graph
     */
    ServiceGraph root();

    /**
     * Find a service by its {@link java.lang.Class} in the graph.
     * Trigger a full traversal of the graph.
     * @param path a non-null class
     * @return the sub-graph containing the service
     */
    @Nullable ServiceGraph get(ServicePath path);

    /**
     * Modify a service's dependency.
     * @param path the service to rewire
     * @param newDep the new dependency
     */
    void rewire(ServicePath path, @Nullable ServicePath newDep);

    default Service fold() {
        return new Service() {
            @Override
            public ServicePath path() {
                return ServicePath.root();
            }

            @Override
            public ServicePath dependsOn() {
                return null;
            }

            @Override
            public void start(StartReason reason) {
                sink((parent, service) -> service.start(reason));
            }

            @Override
            public void stop() {
                emerge((parent, service) -> service.stop());
            }
        };
    }
}
