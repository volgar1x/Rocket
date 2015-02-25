package org.rocket.network.props;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.InOrder;
import org.rocket.network.MutProp;
import org.rocket.network.NetworkClient;
import org.rocket.network.PropId;
import org.rocket.network.PropValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropValidationsTest {
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
        AnnotatedElement target = PropValidationsTest.class.getDeclaredMethod("annotated");
        PropValidatorInstantiator ins = PropValidations::reflectiveInstantiator;

        // when
        List<PropValidator> validators = PropValidations.fetchValidators(target, ins);

        // then
        assertThat(validators.size(), equalTo(2));
    }

    @Test
    public void testValidateSuccess() throws Exception {
        // given
        NetworkClient client = mock(NetworkClient.class);
        AnnotatedElement target = PropValidationsTest.class.getDeclaredMethod("annotated");
        List<PropValidator> validators = PropValidations.fetchValidators(target, PropValidations::reflectiveInstantiator);
        PropValidator validator = PropValidator.aggregate(ImmutableList.copyOf(validators));

        @SuppressWarnings("unchecked")
        MutProp<Object> strProp = mock(MutProp.class),
                intProp = mock(MutProp.class);
        PropId strpid = PropIds.type(String.class),
                intpid = PropIds.type(Integer.class);

        // when
        when(strProp.isDefined()).thenReturn(true);
        when(intProp.isDefined()).thenReturn(true);

        when(client.getProp(strpid)).thenReturn(strProp);
        when(client.getProp(intpid)).thenReturn(intProp);

        validator.validate(client);

        // then
        InOrder o = inOrder(client, strProp, intProp);
        o.verify(client).getProp(strpid);
        o.verify(strProp).isDefined();
        o.verify(client).getProp(intpid);
        o.verify(intProp).isDefined();
        o.verifyNoMoreInteractions();
    }

    @Test(expected = AssertionError.class)
    public void testValidateFailure() throws Exception {
        // given
        NetworkClient client = mock(NetworkClient.class);
        AnnotatedElement target = PropValidationsTest.class.getDeclaredMethod("annotated");
        List<PropValidator> validators = PropValidations.fetchValidators(target, PropValidations::reflectiveInstantiator);
        PropValidator validator = PropValidator.aggregate(ImmutableList.copyOf(validators));

        @SuppressWarnings("unchecked")
        MutProp<Object> strProp = mock(MutProp.class),
                intProp = mock(MutProp.class);

        // when
        when(strProp.isDefined()).thenReturn(true);
        when(intProp.isDefined()).thenReturn(false);

        when(client.getProp(PropIds.type(String.class))).thenReturn(strProp);
        when(client.getProp(PropIds.type(Integer.class))).thenReturn(intProp);

        validator.validate(client);

        // then
        fail();

    }
}