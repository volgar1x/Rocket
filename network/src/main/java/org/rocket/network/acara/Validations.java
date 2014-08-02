package org.rocket.network.acara;

import com.google.inject.Key;
import org.rocket.network.PropBag;

public final class Validations {
    private Validations() {}

    public interface Validation {
        boolean validate(PropBag bag);
        String describe();
    }

    public static class IsPresentValidation implements Validation {
        private final Key<?> key;
        private final boolean not;

        public IsPresentValidation(Key<?> key, boolean not) {
            this.key = key;
            this.not = not;
        }

        @Override
        public boolean validate(PropBag bag) {
            return bag.isPropPresent(key) != not;
        }

        @Override
        public String describe() {
            return "prop " + key + " must " + (not ? "not" : "") + " be present";
        }
    }
}
