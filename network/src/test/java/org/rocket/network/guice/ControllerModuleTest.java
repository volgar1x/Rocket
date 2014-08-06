package org.rocket.network.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rocket.network.*;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControllerModuleTest {

    @Inject ControllerFactory factory;

    @org.rocket.network.Controller
    public static class Controller {
        @Inject NetworkClient client;
        @Inject Helper helper;
        @Inject Prop<String> ticket;
    }

    @org.rocket.network.Controller
    public static class AnotherController {
        @Inject NetworkClient client;
        @Inject MutProp<String> ticket;
    }

    public static class Helper {
        @Inject NetworkClient client;
        @Inject MutProp<String> ticket;
    }

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(
                new ControllerFactoryModule(),
                new ControllerModule() {
                    @Override
                    protected void configure() {
                        newController().to(Controller.class);
                        newProp(String.class);
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
        when(client.getMutProp(any())).thenReturn(new DefaultMutProp<>(Optional.empty()));
        Set<Object> controllers = factory.create(client);

        // then
        assertEquals("number of controllers", 2, controllers.size());

        Iterator<Object> it = controllers.iterator();

        Controller controller = (Controller) it.next();
        assertEquals("controller's client", client, controller.client);
        assertEquals("controller's helper's client", client, controller.helper.client);
        assertEquals("controller's ticket", Optional.<String>empty(), controller.ticket.tryGet());
        assertEquals("controller's helper's ticket", Optional.<String>empty(), controller.helper.ticket.tryGet());
        controller.helper.ticket.set("hello, world!");
        assertEquals("controller's ticket", "hello, world!", controller.ticket.get());
        assertEquals("controller's helper's ticket", "hello, world!", controller.helper.ticket.get());

        AnotherController anotherController = (AnotherController) it.next();
        assertEquals("another controller's client", client, anotherController.client);
        assertEquals("another controller's ticket", "hello, world!", anotherController.ticket.get());
    }
}