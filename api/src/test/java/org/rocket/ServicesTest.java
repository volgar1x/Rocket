package org.rocket;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.rocket.JUnitMatchers.isEmpty;
import static org.rocket.Services.*;

import org.mockito.InOrder;
import org.rocket.Services.*;

import java.util.Optional;

@RunWith(JUnit4.class)
public class ServicesTest {

	@Test
	public void createEmptyGraph() {
		Graph graph = createGraph(ImmutableSet.of());
		assertThat(graph, notNullValue());
	}

	@Test
	public void createSimpleGraph() {
		ServiceContext ctx = mock(ServiceContext.class);

		Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn(Optional.empty());
		when(serviceB.dependsOn()).thenReturn(Optional.empty());


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		graph.forEach((parent, service) -> {
			assertThat(parent, isEmpty());
			assertThat(service, notNullValue());

			service.start(ctx);
		});

		graph.forEachBackwards((parent, service) -> {
			service.stop(ctx);
		});


		verify(serviceA).start(ctx);
		verify(serviceB).start(ctx);
		verify(serviceA).stop(ctx);
		verify(serviceB).stop(ctx);
	}

	@Test
	public void createGraphWithOneParent() {
		ServiceContext ctx = mock(ServiceContext.class);

		Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn(Optional.empty());
		when(serviceB.dependsOn()).thenReturn(Optional.of(serviceA.getClass()));


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		graph.forEach((parent, service) -> {
			assertThat(service, notNullValue());

			service.start(ctx);
		});

		graph.forEachBackwards((parent, service) -> {
			service.stop(ctx);
		});


		InOrder inOrder = inOrder(serviceA, serviceB);

		inOrder.verify(serviceA).start(ctx);
		inOrder.verify(serviceB).start(ctx);
		inOrder.verify(serviceB).stop(ctx);
		inOrder.verify(serviceA).stop(ctx);
	}

	@Test
	public void createGraphWithRing() {
		ServiceContext ctx = mock(ServiceContext.class);

		Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn(Optional.of(serviceB.getClass()));
		when(serviceB.dependsOn()).thenReturn(Optional.of(serviceA.getClass()));


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		graph.forEach((parent, service) -> {
			assertThat(service, notNullValue());

			service.start(ctx);
		});

		graph.forEachBackwards((parent, service) -> {
			service.stop(ctx);
		});


		verify(serviceA, never()).start(ctx);
		verify(serviceB, never()).start(ctx);
		verify(serviceA, never()).stop(ctx);
		verify(serviceB, never()).stop(ctx);
	}

	@Test
	public void graphFolding() {
		ServiceContext ctx = mock(ServiceContext.class);

		Service serviceA = mock(Service.class),
				serviceB = mock(Service.class);

		when(serviceA.dependsOn()).thenReturn(Optional.empty());
		when(serviceB.dependsOn()).thenReturn(Optional.of(serviceA.getClass()));


		Graph graph = createGraph(ImmutableSet.of(serviceA, serviceB));

		Service folded = graph.fold();

		folded.start(ctx);
		folded.stop(ctx);


		assertThat(folded.dependsOn(), isEmpty());


		InOrder inOrder = inOrder(serviceA, serviceB);

		inOrder.verify(serviceA).start(ctx);
		inOrder.verify(serviceB).start(ctx);
		inOrder.verify(serviceB).stop(ctx);
		inOrder.verify(serviceA).stop(ctx);
	}

}
