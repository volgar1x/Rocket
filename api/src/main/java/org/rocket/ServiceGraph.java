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
     * @param klass a non-null class
     * @return the sub-graph containing the service
     */
    @Nullable ServiceGraph get(Class<?> klass);

    /**
     * Modify a service's dependency.
     * @param klass the service to rewire
     * @param newDep the new dependency
     */
    void rewire(Class<?> klass, @Nullable Class<?> newDep);
}
