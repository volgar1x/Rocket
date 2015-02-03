package org.rocket;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link java.util.Optional} for free :o)
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_USE)
public @interface Nullable {
}
