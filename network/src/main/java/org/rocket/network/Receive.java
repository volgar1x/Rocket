package org.rocket.network;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Receive {
    boolean acceptsChildren() default false;
}
