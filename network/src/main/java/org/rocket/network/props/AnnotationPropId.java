package org.rocket.network.props;

import org.rocket.network.PropId;

import java.lang.annotation.Annotation;

final class AnnotationPropId implements PropId {
    final Annotation annotation;

    AnnotationPropId(Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotationPropId that = (AnnotationPropId) o;
        return annotation.equals(that.annotation);
    }

    @Override
    public int hashCode() {
        return annotation.hashCode();
    }

    @Override
    public String toString() {
        return "AnnotationPropId{" +
                "annotation=" + annotation +
                '}';
    }
}
