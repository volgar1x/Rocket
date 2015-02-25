package org.rocket.network.acara;

import org.rocket.network.PropValidate;
import org.rocket.network.PropValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

final class Validations {
    private Validations() {}

    static interface Instantiator {
        PropValidator<?> instantiate(Class<PropValidator<?>> klass) throws Exception;
    }

    public static List<PropValidator<?>> fetchValidators(AnnotatedElement ele, Instantiator ins) {
        List<PropValidator<?>> validators = new ArrayList<>();
        Deque<AnnotatedElement> stack = new LinkedList<>();
        fetchValidators(ele, ins, validators, stack);
        return validators;
    }

    private static void fetchValidators(AnnotatedElement ele, Instantiator ins, List<PropValidator<?>> validators, Deque<AnnotatedElement> stack) {
        stack.addLast(ele);
        try {
            for (Annotation annotation : ele.getAnnotations()) {
                if (stack.contains(annotation.annotationType())) {
                    continue;
                }

                if (annotation.annotationType() == PropValidate.class) {
                    PropValidate ann = (PropValidate) annotation;

                    @SuppressWarnings("unchecked")
                    PropValidator<?> validator = ins.instantiate((Class) ann.value());

                    validators.add(validator);
                } else {
                    fetchValidators(annotation.annotationType(), ins, validators, stack);
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        } finally {
            stack.removeLast();
        }
    }
}
