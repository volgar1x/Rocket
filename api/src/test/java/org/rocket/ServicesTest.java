package org.rocket;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.rocket.Services.Graph;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.rocket.JUnitMatchers.isEmpty;
import static org.rocket.Services.createGraph;

@RunWith(JUnit4.class)
public class ServicesTest {

	@Test
	public void createEmptyGraph() {
		Graph graph = createGraph(ImmutableSet.of());
		assertThat(graph, notNullValue());
	}

	@Test
	public void createSimpleGraph() {
        Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn(null);
		when(serviceB.dependsOn()).thenReturn(null);


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		graph.forEach((parent, service) -> {
			assertThat(parent, isEmpty());
			assertThat(service, notNullValue());

			service.start(StartReason.NORMAL);
		});

		graph.forEachBackwards((parent, service) -> service.stop());


		verify(serviceA).start(StartReason.NORMAL);
		verify(serviceB).start(StartReason.NORMAL);
		verify(serviceA).stop();
		verify(serviceB).stop();
	}

	@SuppressWarnings("unchecked")
    @Test
	public void createGraphWithOneParent() {
        Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn(null);
		when(serviceB.dependsOn()).thenReturn((Class) serviceA.getClass());


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		graph.forEach((parent, service) -> {
			assertThat(service, notNullValue());

			service.start(StartReason.NORMAL);
		});

		graph.forEachBackwards((parent, service) -> service.stop());


		InOrder inOrder = inOrder(serviceA, serviceB);

		inOrder.verify(serviceA).start(StartReason.NORMAL);
		inOrder.verify(serviceB).start(StartReason.NORMAL);
		inOrder.verify(serviceB).stop();
		inOrder.verify(serviceA).stop();
	}

	@SuppressWarnings("unchecked")
    @Test
	public void createGraphWithRing() {
        Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn((Class) serviceB.getClass());
		when(serviceB.dependsOn()).thenReturn((Class) serviceA.getClass());


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		graph.forEach((parent, service) -> {
			assertThat(service, notNullValue());

			service.start(StartReason.NORMAL);
		});

		graph.forEachBackwards((parent, service) -> service.stop());


		verify(serviceA, never()).start(StartReason.NORMAL);
		verify(serviceB, never()).start(StartReason.NORMAL);
		verify(serviceA, never()).stop();
		verify(serviceB, never()).stop();
	}

	@SuppressWarnings("unchecked")
    @Test
	public void graphFolding() {
        Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn(null);
		when(serviceB.dependsOn()).thenReturn((Class) serviceA.getClass());


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		Service folded = graph.fold();

		folded.start(StartReason.NORMAL);
		folded.stop();


		assertThat(folded.dependsOn(), nullValue());


		InOrder inOrder = inOrder(serviceA, serviceB);

		inOrder.verify(serviceA).start(StartReason.NORMAL);
		inOrder.verify(serviceB).start(StartReason.NORMAL);
		inOrder.verify(serviceB).stop();
		inOrder.verify(serviceA).stop();
	}

}
