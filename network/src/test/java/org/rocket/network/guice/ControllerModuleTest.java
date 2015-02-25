package org.rocket.network.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rocket.network.*;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControllerModuleTest {

    @Inject ControllerFactory factory;

    @Controller
    public static class TheController {
        @Inject NetworkClient client;
        @Inject MutProp<String> name;
        @Inject Helper helper;
    }

    @Controller
    public static class AnotherController {
        @Inject NetworkClient client;
        @Inject Prop<String> name;
    }

    public static class Helper {
        @Inject NetworkClient client;
    }

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(
                new ControllerFactoryModule(),
                new ControllerModule() {
                    @Override
                    protected void configure() {
                        newController(TheController.class);
                        newProp(String.class);
                    }
                },
                new ControllerModule() {
                    @Override
                    protected void configure() {
                        newController(AnotherController.class);
                    }
                }
        );
        injector.injectMembers(this);
    }

    @After
    public void tearDown() throws Exception {
        factory = null;
    }

    @Test
    public void testCreateControllers() throws Exception {
        // given
        NetworkClient client = mock(NetworkClient.class);

        @SuppressWarnings("unchecked")
        MutProp<Object> prop = mock(MutProp.class);
        when(client.getProp(any())).thenReturn(prop);

        // when
        Set<Object> controllers = factory.create(client);

        // then
        assertEquals("number of controllers", 2, controllers.size());

        Iterator<Object> it = controllers.iterator();

        TheController controller = (TheController) it.next();
        assertEquals("controller's client", client, controller.client);
        assertEquals("controller's helper's client", client, controller.helper.client);

        AnotherController anotherController = (AnotherController) it.next();
        assertEquals("another controller's client", client, anotherController.client);

        assertThat("controllers' props", controller.name, equalTo(prop));
        assertThat("controllers' props", anotherController.name, equalTo(prop));
    }
}