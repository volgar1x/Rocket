package org.rocket;

import java.util.*;
import java.util.function.BiConsumer;

public final class Services {
	private Services() {}

    public static ServiceGraph newGraph(Collection<Service> services) {
        return newGraphInternal(services);
    }

    static Graph newGraphInternal(Collection<Service> services) {
        // for test only
        List<Service> newServices = new LinkedList<>(services);
        Graph graph = root();
        populateGraph(graph, newServices);
        if (!newServices.isEmpty()) {
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

    static void populateGraph(Graph parent, List<Service> services) {
        for (ListIterator<Service> it = services.listIterator(); it.hasNext(); ) {
            Service service = it.next();

            if (parent.accepts(service)) {
                it.remove();

                Graph graph = graph(parent, service);
                populateGraph(graph, services);

                parent.children.add(graph);
            }
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
        public @Nullable Graph get(Class<?> klass) {
            if (klass.isInstance(item)) {
                return this;
            }

            for (Graph child : children) {
                @Nullable Graph found = child.get(klass);
                if (found != null) {
                    return found;
                }
            }

            return null;
        }

        @Override
        public void rewire(Class<?> klass, @Nullable Class<?> newDep) {
            Graph subgraph = get(klass);
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
