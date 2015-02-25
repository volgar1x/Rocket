package org.rocket.network.props;

import org.rocket.network.PropId;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public final class PropIds {
    private PropIds() {}

    public static PropId type(Type type) {
        return new TypePropId(type);
    }

    public static PropId annotation(Annotation annotation) {
        return new AnnotationPropId(annotation);
    }

    public static PropId of(Iterable<PropId> pids) {
        ListPropId acc = ListPropId.Nil;
        for (PropId pid : pids) {
            acc = ListPropId.cons(pid, acc);
        }
        return acc;
    }

    public static PropId of(PropId... pids) {
        ListPropId acc = ListPropId.Nil;
        for (PropId pid : pids) {
            acc = ListPropId.cons(pid, acc);
        }
        return acc;
    }
}
