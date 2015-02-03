package org.rocket;

/**
 * A ServicePath describes the location of a {@link org.rocket.Service}.
 */
public final class ServicePath {
    private final String path;

    ServicePath(String path) {
        this.path = path;
    }

    public static ServicePath root() {
        return new ServicePath("");
    }

    public static ServicePath absolute(String path) {
        return new ServicePath(path);
    }

    public static ServicePath of(Object service) {
        return absolute(service.getClass().getName());
    }

    public boolean match(ServicePath other) {
        return this.path.equalsIgnoreCase(other.path);
    }

    public ServicePath concat(ServicePath child) {
        return new ServicePath(path + "/" + child.path);
    }

    @Override
    public String toString() {
        return "/" + path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServicePath that = (ServicePath) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
