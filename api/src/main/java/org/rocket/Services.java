package org.rocket;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Various utilities to use and manipulate services. Typically a companion object for {@link org.rocket.Service}.
 * @see org.rocket.ServiceGraph
 * @see org.rocket.Service
 */
public final class Services {
	private Services() {}

    /**
     * A virtual service does nothing. It just has a path and can depend on another service.
     * @param path a path describing the service
     * @param dependsOn a optional path on which the service should depend
     * @return a service doing nothing
     */
    public static Service virtual(ServicePath path, @Nullable ServicePath dependsOn) {
        return new Service() {
            @Override public ServicePath path()      { return path; }
            @Override public ServicePath dependsOn() { return dependsOn; }
            @Override public void start(StartReason reason) { }
            @Override public void stop() { }
        };
    }

    /**
     * Create a graph of services according to their dependencies.
     * @param services a non-structured collection of services
     * @return the built graph of services
     */
    public static ServiceGraph newGraph(Set<Service> services) {
        return newGraphInternal(services);
    }

    static Graph newGraphInternal(Set<Service> services) {
        // for test only
        Deque<Service> stack = new LinkedList<>(services);
        Graph graph = root();
        populateGraph(graph, stack);
        if (!stack.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return graph;
    }

    static Graph root() {
        return new Graph(null, null, new HashSet<>());
    }

    static Graph graph(Graph parent, Service item) {
        return new Graph(parent, item, new HashSet<>());
    }

    static void populateGraph(Graph parent, Deque<Service> stack) {
        int maxIterations = stack.size();
        while (!stack.isEmpty() && maxIterations > 0) {
            Service service = stack.removeFirst();
            maxIterations--;

            if (!parent.accepts(service)) {
                stack.addLast(service);
                continue;
            }

            Graph graph = graph(parent, service);
            populateGraph(graph, stack);
            parent.children.add(graph);
        }
    }

    final static class Graph implements ServiceGraph {
        final @Nullable Service item;
        @Nullable Graph parent;
        final Set<Graph> children;

        Graph(@Nullable Graph parent, @Nullable Service item, Set<Graph> children) {
            this.parent = parent;
            this.item = item;
            this.children = children;
        }

        boolean accepts(Service child) {
            if (item == null) {
                return child.dependsOn() == null;
            }
            return item.path().match(child.dependsOn());
        }

        @Override
        public void sink(BiConsumer<@Nullable Service, Service> fn) {
            if (parent != null && item != null) {
                fn.accept(parent.item, item);
            }
            children.forEach(child -> child.sink(fn));
        }

        @Override
        public void emerge(BiConsumer<@Nullable Service, Service> fn) {
            children.forEach(child -> child.emerge(fn));
            if (parent != null && item != null) {
                fn.accept(parent.item, item);
            }
        }

        @Override
        public Graph root() {
            return parent != null ? parent.root() : this;
        }

        @Override
        public @Nullable Graph get(ServicePath path) {
            if (item != null && path.match(item.path())) {
                return this;
            }

            for (Graph child : children) {
                @Nullable Graph found = child.get(path);
                if (found != null) {
                    return found;
                }
            }

            return null;
        }

        @Override
        public void rewire(ServicePath path, @Nullable ServicePath newDep) {
            Graph subgraph = get(path);
            if (subgraph == null) {
                // try to rewire a service not contained in the graph
                // ignore it till we have some reason to fail
                return;
            }
            assert subgraph.parent != null && subgraph.item != null;

            Graph newParent = newDep != null ? get(newDep) : root();
            if (newParent == null) {
                // fail if wanted to rewire to a non-existent service
                throw new NoSuchElementException();
            }

            subgraph.parent.children.remove(subgraph);
            subgraph.parent = newParent;
            newParent.children.add(subgraph);
        }
    }

}
