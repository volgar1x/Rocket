package org.rocket.network.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rocket.network.ControllerFactory;
import org.rocket.network.NetworkClient;

import javax.inject.Inject;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class GuiceControllerModuleTest {

    @Inject ControllerFactory factory;

    @org.rocket.network.Controller
    public static class Controller {
        @Inject NetworkClient client;
        @Inject Helper helper;
    }

    public static class Helper {
        @Inject NetworkClient client;
    }

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(
                new GuiceControllerModule() {
                    @Override
                    protected void configure() {
                        newController().to(Controller.class);
                        newHelper(Helper.class);
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
        assertEquals("number of controllers", 1, controllers.size());

        Controller controller = (Controller) controllers.iterator().next();
        assertEquals("controller's client", client, controller.client);
        assertEquals("controller's helper's client", client, controller.helper.client);
    }
}