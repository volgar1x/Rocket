package org.rocket.network;

public interface PropValidator<T> {
    void validate(Prop<T> prop);
}
