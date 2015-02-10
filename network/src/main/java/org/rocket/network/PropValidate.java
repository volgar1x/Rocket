package org.rocket.network;

public @interface PropValidate {
    Class<? extends PropValidator> validator() default NullValidator.class;

    public static class NullValidator implements PropValidator<Object> {
        @Override public void validate(Prop<Object> prop) { }
    }
}
