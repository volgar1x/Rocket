package org.rocket;

import org.junit.Test;
import org.mockito.InOrder;
import org.rocket.Services.Graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.rocket.ServicePath.absolute;

public class ServiceGraphTest {
    protected Services.Graph newGraph(Service... services) {
        Set<Service> res = new HashSet<>();
        Collections.addAll(res, services);
        return Services.newGraphInternal(res);
    }

    Service mockService(ServicePath path, @Nullable ServicePath dep) {
        Service service = mock(Service.class);
        when(service.path()).thenReturn(path);
        when(service.dependsOn()).thenReturn(dep);
        return service;
    }

    @Test
    public void testNewGraph() throws Exception {
        Service a = mockService(absolute("A"), null),
                b = mockService(absolute("B"), absolute("A"));

        Graph graph = newGraph(a, b);

        graph.sink((parent, s) -> s.start(StartReason.NORMAL));
        graph.emerge((parent, s) -> s.stop());

        InOrder o = inOrder(a, b);
        o.verify(a).start(StartReason.NORMAL);
        o.verify(b).start(StartReason.NORMAL);
        o.verify(b).stop();
        o.verify(a).stop();
        o.verifyNoMoreInteractions();
    }

    @Test
    public void testFold() throws Exception {
        Service a = mockService(absolute("A"), null),
                b = mockService(absolute("B"), absolute("A"));

        Graph graph = newGraph(a, b);
        Service service = graph.fold();

        service.start(StartReason.NORMAL);
        service.stop();

        InOrder o = inOrder(a, b);
        o.verify(a).start(StartReason.NORMAL);
        o.verify(b).start(StartReason.NORMAL);
        o.verify(b).stop();
        o.verify(a).stop();
        o.verifyNoMoreInteractions();
    }

    @Test
    public void testRewire() throws Exception {
        Service a = mockService(absolute("A"), null),
                b = mockService(absolute("B"), absolute("A"));

        Graph graph = newGraph(a, b);

        Graph graph1 = graph.get(absolute("A"));
        Graph graph2 = graph.get(absolute("B"));
        assert graph1 != null;
        assert graph2 != null;

        // root -> A -> B
        assertThat(graph1.parent, equalTo(graph));
        assertThat(graph2.parent, equalTo(graph1));

        graph.rewire(absolute("B"), null);

        // root -> A
        // root -> B
        assertThat(graph1.parent, equalTo(graph));
        assertThat(graph2.parent, equalTo(graph));

        graph.rewire(absolute("A"), absolute("B"));

        // root -> B -> A
        assertThat(graph1.parent, equalTo(graph2));
        assertThat(graph2.parent, equalTo(graph));

        graph.rewire(absolute("UNKNOWN SERVICE"), null);

        // root -> B -> A
        assertThat(graph1.parent, equalTo(graph2));
        assertThat(graph2.parent, equalTo(graph));

        try {
            graph.rewire(absolute("A"), absolute("UNKNOWN SERVICE"));
            fail("successfully rewired to an unknown service");
        } catch (NoSuchElementException ignored) {

        }
        // root -> B -> A
        assertThat(graph1.parent, equalTo(graph2));
        assertThat(graph2.parent, equalTo(graph));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCircularGraph() throws Exception {
        Service a = mockService(absolute("A"), absolute("B")),
                b = mockService(absolute("B"), absolute("A"));

        newGraph(a, b);
    }

    @Test
    public void testRoot() throws Exception {
        Service a = mockService(absolute("A"), null),
                b = mockService(absolute("B"), absolute("A"));

        Graph graph = newGraph(a, b);

        Graph bottomest = graph.get(absolute("B"));
        assert bottomest != null;
        Graph root = bottomest.root();

        assertThat(root, equalTo(graph));
    }
}
