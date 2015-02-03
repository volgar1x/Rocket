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

public class ServiceGraphTest {
    protected Services.Graph newGraph(Service... services) {
        Set<Service> res = new HashSet<>();
        Collections.addAll(res, services);
        return Services.newGraphInternal(res);
    }

    class MockService implements Service {
        final Class<?> dependency;
        MockService(Class<?> dependency) { this.dependency = dependency; }

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends Service> dependsOn() { return (Class) dependency; }

        @Override public void start(StartReason reason) { }

        @Override public void stop() { }
    }

    class A extends MockService {
        A() { super(null); }
    }

    class B extends MockService {
        B() { super(A.class); }
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
