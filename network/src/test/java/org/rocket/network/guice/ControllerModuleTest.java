package org.rocket.network.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rocket.network.*;

import javax.inject.Inject;
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

    public static class Helper {
        @Inject NetworkClient client;
        @Inject MutProp<String> ticket;
    }

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(
                new ControllerModule() {
                    @Override
                    protected void configure() {
                        newController().to(Controller.class);
                        newHelper(Helper.class);
                        newProp(String.class);
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
        assertEquals("number of controllers", 1, controllers.size());

        Controller controller = (Controller) controllers.iterator().next();
        assertEquals("controller's client", client, controller.client);
        assertEquals("controller's helper's client", client, controller.helper.client);
        assertEquals("controller's ticket", Optional.<String>empty(), controller.ticket.tryGet());
        assertEquals("controller's helper's ticket", Optional.<String>empty(), controller.helper.ticket.tryGet());
        controller.helper.ticket.set("hello, world!");
        assertEquals("controller's ticket", "hello, world!", controller.ticket.get());
        assertEquals("controller's helper's ticket", "hello, world!", controller.helper.ticket.get());
    }
}