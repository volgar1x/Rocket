package org.rocket.network;

public @interface PropPresence {

    String NULL = "you should never ever use this string as the key of a prop! no srsly don't! i warned you!";

    String key() default NULL;
}
