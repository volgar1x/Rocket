package org.rocket.network.acara;

import org.junit.Test;
import org.rocket.network.PropValidation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RocketAcaraTest {

    @PropValidation(value = Byte.class, present = true)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnotherAnnotation {}

    @PropValidation(value = String.class, present = true)
    @PropValidation(value = Integer.class, present = true)
    @AnotherAnnotation
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SomeAnnotation {}

    @SomeAnnotation
    public static class Hook {}

    @Test
    public void testLookupPropAnnotations() throws Exception {
        List<Validations.Validation> validations = RocketAcara.lookupPropValidations(Hook.class);

        assertEquals("number of validations", 3, validations.size());
    }
}