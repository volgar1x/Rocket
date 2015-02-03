package org.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public final class Services {
	private Services() {}

	public static interface GraphConsumer {
		void accept(Optional<Service> parent, Service service);
	}

	public static interface Graph {
		void forEach(GraphConsumer fn);
		void forEachBackwards(GraphConsumer fn);

		default Service fold() {
			return new Service() {
				@Override
				public Class<? extends Service> dependsOn() {
					return null;
				}

				@Override
				public void start(StartReason reason) {
					forEach((parent, service) -> service.start(StartReason.NORMAL));
				}

				@Override
				public void stop() {
					forEachBackwards((parent, service) -> service.stop());
				}
			};
		}
	}

	static class Root implements Graph {
		final List<Node> children = new ArrayList<>();

		@Override
		public void forEach(GraphConsumer fn) {
			for (Node child : children) {
				fn.accept(empty(), child.parent);
				child.forEach(fn);
			}
		}

		@Override
		public void forEachBackwards(GraphConsumer fn) {
			for (Node child : children) {
				child.forEachBackwards(fn);
				fn.accept(empty(), child.parent);
			}
		}
	}

	static class Node implements Graph {
		final Service parent;
		final List<Node> children = new ArrayList<>();

		Node(Service parent) {
			this.parent = requireNonNull(parent, "parent");
		}

		@Override
		public void forEach(GraphConsumer fn) {
			Optional<Service> parentOpt = of(parent);

			for (Node child : children) {
				fn.accept(parentOpt, child.parent);
				child.forEach(fn);
			}
		}

		@Override
		public void forEachBackwards(GraphConsumer fn) {
			Optional<Service> parentOpt = of(parent);

			for (Node child : children) {
				child.forEachBackwards(fn);
				fn.accept(parentOpt, child.parent);
			}
		}
	}

	public static Graph createGraph(Set<Service> s) {
		Root root = new Root();

		List<Node> nodes = s.stream().map(Node::new).collect(Collectors.toList());

		for (Node node : nodes) {
			Class<? extends Service> klass = node.parent.dependsOn();

			if (klass == null) {
				root.children.add(node);
			} else {
				Node parent = nodes.stream().filter(x -> klass.isInstance(x.parent)).findAny()
						.orElseThrow(() -> new IllegalStateException("unresolved service " + klass));

				parent.children.add(node);
			}
		}

		return root;
	}
}
