package org.rocket.network.props;

import org.rocket.network.PropValidate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@PropValidate(PropPresenceValidator.class)
public @interface PropPresence {
    Class<?> value();
}
