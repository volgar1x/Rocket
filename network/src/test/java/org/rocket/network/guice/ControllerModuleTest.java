package org.rocket.network.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rocket.network.Controller;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ControllerModuleTest {

    @Inject ControllerFactory factory;

    @Controller
    public static class TheController {
        @Inject NetworkClient client;
        @Inject Helper helper;
    }

    @Controller
    public static class AnotherController {
        @Inject NetworkClient client;
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
                        newController().to(TheController.class);
                    }
                },
                new ControllerModule() {
                    @Override
                    protected void configure() {
                        newController().to(AnotherController.class);
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
    }
}