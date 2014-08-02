package org.rocket.network;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(PropValidations.class)
public @interface PropValidation {
    Class<?> value();
    boolean present();
}
