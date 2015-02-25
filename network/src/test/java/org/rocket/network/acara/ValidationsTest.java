package org.rocket.network.acara;

import org.junit.Test;
import org.rocket.network.PropValidator;
import org.rocket.network.props.PropPresence;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ValidationsTest {
    @Retention(RetentionPolicy.RUNTIME)
    @PropPresence(String.class)
    @interface FooBar {}

    @SuppressWarnings("UnusedDeclaration")
    @FooBar
    @PropPresence(Integer.class)
    private static void annotated() {}

    @Test
    public void testFetchValidators() throws Exception {
        // given
        AnnotatedElement target = ValidationsTest.class.getDeclaredMethod("annotated");
        Validations.Instantiator ins = Class::newInstance;

        // when
        List<PropValidator<?>> validators = Validations.fetchValidators(target, ins);

        // then
        assertThat(validators.size(), equalTo(2));
    }
}