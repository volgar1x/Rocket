package org.rocket;

import org.junit.Test;
import org.mockito.InOrder;
import org.rocket.Services.Graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.rocket.ServicePath.absolute;

public class ServiceGraphTest {
    protected Services.Graph newGraph(Service... services) {
        Set<Service> res = new HashSet<>();
        Collections.addAll(res, services);
        return Services.newGraphInternal(res);
    }

    class MockService implements Service {
        final ServicePath path, dependency;
        MockService(ServicePath path, ServicePath dependency) {
            this.path = path;
            this.dependency = dependency;
        }

        @Override
        public ServicePath path() {
            return path;
        }

        @Override
        public ServicePath dependsOn() { return dependency; }

        @Override public void start(StartReason reason) { }

        @Override public void stop() { }
    }

    class A extends MockService {
        A() { super(absolute("A"), null); }
    }

    class B extends MockService {
        B() { super(absolute("B"), absolute("A")); }
    }

    @Test
    public void testNewGraph() throws Exception {
        Service a = spy(new A()), b = spy(new B());

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
    public void testRewire() throws Exception {
        Service a = spy(new A()), b = spy(new B());

        Graph graph = newGraph(a, b);

        Graph graph1 = graph.get(a.getClass());
        Graph graph2 = graph.get(b.getClass());
        assert graph1 != null;
        assert graph2 != null;

        assertThat(graph1.parent, equalTo(graph));
        assertThat(graph2.parent, equalTo(graph1));

        graph.rewire(b.getClass(), null);

        assertThat(graph1.parent, equalTo(graph));
        assertThat(graph2.parent, equalTo(graph));

        graph.rewire(a.getClass(), b.getClass());

        assertThat(graph1.parent, equalTo(graph2));
        assertThat(graph2.parent, equalTo(graph));
    }
}
