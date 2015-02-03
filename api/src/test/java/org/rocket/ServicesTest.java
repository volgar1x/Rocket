package org.rocket;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.rocket.Services.Graph;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
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

		when(serviceA.dependsOn()).thenReturn(Optional.empty());
		when(serviceB.dependsOn()).thenReturn(Optional.empty());


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		graph.forEach((parent, service) -> {
			assertThat(parent, isEmpty());
			assertThat(service, notNullValue());

			service.start();
		});

		graph.forEachBackwards((parent, service) -> {
			service.stop();
		});


		verify(serviceA).start();
		verify(serviceB).start();
		verify(serviceA).stop();
		verify(serviceB).stop();
	}

	@Test
	public void createGraphWithOneParent() {
        Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn(Optional.empty());
		when(serviceB.dependsOn()).thenReturn(Optional.of(serviceA.getClass()));


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		graph.forEach((parent, service) -> {
			assertThat(service, notNullValue());

			service.start();
		});

		graph.forEachBackwards((parent, service) -> {
			service.stop();
		});


		InOrder inOrder = inOrder(serviceA, serviceB);

		inOrder.verify(serviceA).start();
		inOrder.verify(serviceB).start();
		inOrder.verify(serviceB).stop();
		inOrder.verify(serviceA).stop();
	}

	@Test
	public void createGraphWithRing() {
        Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn(Optional.of(serviceB.getClass()));
		when(serviceB.dependsOn()).thenReturn(Optional.of(serviceA.getClass()));


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		graph.forEach((parent, service) -> {
			assertThat(service, notNullValue());

			service.start();
		});

		graph.forEachBackwards((parent, service) -> {
			service.stop();
		});


		verify(serviceA, never()).start();
		verify(serviceB, never()).start();
		verify(serviceA, never()).stop();
		verify(serviceB, never()).stop();
	}

	@Test
	public void graphFolding() {
        Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn(Optional.empty());
		when(serviceB.dependsOn()).thenReturn(Optional.of(serviceA.getClass()));


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		Service folded = graph.fold();

		folded.start();
		folded.stop();


		assertThat(folded.dependsOn(), isEmpty());


		InOrder inOrder = inOrder(serviceA, serviceB);

		inOrder.verify(serviceA).start();
		inOrder.verify(serviceB).start();
		inOrder.verify(serviceB).stop();
		inOrder.verify(serviceA).stop();
	}

}
