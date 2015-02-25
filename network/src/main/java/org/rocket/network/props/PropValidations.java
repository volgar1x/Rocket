package org.rocket.network.props;

import org.rocket.network.PropValidate;
import org.rocket.network.PropValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.*;

public final class PropValidations {
    private PropValidations() {}

    public static PropValidator reflectiveInstantiator(Class<? extends PropValidator> klass, Deque<Annotation> stack) throws Exception {
        for (Constructor<?> ctor : klass.getConstructors()) {
            if (ctor.getParameterCount() != 1) {
                continue;
            }

            Class<?> param = ctor.getParameterTypes()[0];
            Annotation ele = firstOfType(stack, param);
            if (ele == null) {
                continue;
            }

            ctor.setAccessible(true);
            return (PropValidator) ctor.newInstance(ele);
        }

        throw new NoSuchElementException();
    }

    public static List<PropValidator> fetchValidators(AnnotatedElement ele, PropValidatorInstantiator ins) {
        List<PropValidator> validators = new ArrayList<>();
        Deque<AnnotatedElement> stack = new LinkedList<>();
        Deque<Annotation> annStack = new LinkedList<>();
        fetchValidators(ele, ins, validators, stack, annStack);
        return validators;
    }

    private static Annotation firstOfType(Deque<Annotation> stack, Class<?> klass) {
        for (Annotation element : stack) {
            if (klass.isInstance(element)) {
                return element;
            }
        }
        return null;
    }

    private static void fetchValidators(AnnotatedElement ele, PropValidatorInstantiator ins, List<PropValidator> validators, Deque<AnnotatedElement> stack, Deque<Annotation> annStack) {
        stack.addLast(ele);
        try {
            for (Annotation annotation : ele.getAnnotations()) {
                if (stack.contains(annotation.annotationType())) {
                    continue;
                }

                annStack.addLast(annotation);
                try {
                    if (annotation.annotationType() == PropValidate.class) {
                        PropValidate ann = (PropValidate) annotation;

                        PropValidator validator = ins.instantiate(ann.value(), annStack);
                        validators.add(validator);
                    } else {
                        fetchValidators(annotation.annotationType(), ins, validators, stack, annStack);
                    }
                } finally {
                    annStack.removeLast();
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
