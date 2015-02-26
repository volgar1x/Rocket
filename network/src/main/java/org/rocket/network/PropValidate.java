package org.rocket.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
public @interface PropValidate {
    Class<? extends PropValidator> value();
    String message() default NIL;

    public static final String NIL = "YOU MUST NEVER USE THIS STRING AS THIS ANNOTATION PARAMETER MESSAGE";
}
