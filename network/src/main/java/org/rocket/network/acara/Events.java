package org.rocket.network.acara;

import com.github.blackrush.acara.EventMetadata;
import com.github.blackrush.acara.TypedEventMetadata;

final class Events {
    private Events() {}

    static final class ConnectEventMetadata extends EventMetadata {
        final boolean disconnecting;

        ConnectEventMetadata(boolean disconnecting) {
            this.disconnecting = disconnecting;
        }

        @Override
        public EventMetadata getParent() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConnectEventMetadata that = (ConnectEventMetadata) o;
            return disconnecting == that.disconnecting;
        }

        @Override
        public int hashCode() {
            return getClass().hashCode() + (disconnecting ? 1 : 0);
        }
    }

    static final class ComponentWiseEventMetadata<T> extends TypedEventMetadata<T> {
        final Class<?> componentClass;

        ComponentWiseEventMetadata(Class<?> componentClass) {
            this.componentClass = componentClass;
        }

        @Override
        public ComponentWiseEventMetadata<T> getParent() {
            Class<?> superclass = componentClass.getSuperclass();
            if (superclass == Object.class) {
                return null;
            }
            return new ComponentWiseEventMetadata<>(superclass);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComponentWiseEventMetadata that = (ComponentWiseEventMetadata) o;
            return componentClass.equals(that.componentClass);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode() + componentClass.hashCode();
        }
    }
}
