package org.rocket;

/**
 * A ServicePath describes the location of a {@link org.rocket.Service}.
 */
public final class ServicePath {
    private final String path;

    ServicePath(String path) {
        this.path = path;
    }

    /**
     * Return a path acting as the root.
     * @return the root path
     */
    public static ServicePath root() {
        return new ServicePath("");
    }

    /**
     * Build an absolute path.
     * @param path a string representation of the path
     * @return the built absolute path
     */
    public static ServicePath absolute(String path) {
        return new ServicePath(path);
    }

    /**
     * Build a path given a service's reference.
     * @param service a service reference
     * @return the built path
     */
    public static ServicePath of(Object service) {
        return absolute(service.getClass().getName());
    }

    /**
     * Determine whether or not a path equals to another.
     * @param other another path
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean match(ServicePath other) {
        return other != null && this.path.equalsIgnoreCase(other.path);
    }

    /**
     * Concat a path with another.
     * Example :
     * <code>
     *     ServicePath pathA = ServicePath.of(A), //=&gt; "/A"
     *                 pathB = ServicePath.of(B); //=&gt; "/B"
     *
     *     ServicePath newPath = pathA.concat(pathB);
     *     println(newPath); //=&gt; "/A/B"
     * </code>
     * @param child the path to append
     * @return the concatenated path
     */
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
