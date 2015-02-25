package org.rocket.network.props;

import org.rocket.network.PropValidator;

import java.lang.annotation.Annotation;
import java.util.Deque;

@FunctionalInterface
public interface PropValidatorInstantiator {
    PropValidator instantiate(Class<? extends PropValidator> klass, Deque<Annotation> stack) throws Exception;
}
