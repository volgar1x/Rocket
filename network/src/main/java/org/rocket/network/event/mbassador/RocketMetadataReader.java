package org.rocket.network.event.mbassador;

import com.google.common.collect.ImmutableMap;
import net.engio.mbassy.common.IPredicate;
import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.listener.*;
import org.rocket.network.event.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static com.google.common.base.Throwables.propagate;

public class RocketMetadataReader extends MetadataReader {

	Map<Class<? extends Annotation>, Class<?>> events = ImmutableMap.<Class<? extends Annotation>, Class<?>>builder()
			.put(Connect.class,    ConnectEvent.class)
			.put(Disconnect.class, DisconnectEvent.class)
			.put(Receive.class,    ReceiveEvent.class)
			.put(Recover.class,    RecoverEvent.class)
			.build();

	Map<Class<? extends Annotation>, IMessageFilter> filters = ImmutableMap.<Class<? extends Annotation>, IMessageFilter>builder()
			.put(Receive.class, (m, meta) -> meta.getHandler().getParameterTypes()[1].isInstance(((ReceiveEvent<?>) m).getMessage()))
			.put(Recover.class, (m, meta) -> meta.getHandler().getParameterTypes()[1].isInstance(((RecoverEvent<?>) m).getError()))
			.build();

	@Override
	public MessageListenerMetadata getMessageListener(Class target) {
		MessageListenerMetadata metadata = super.getMessageListener(target);

		for (Method method : findMethods(target, x -> firstAnnotation(x, events.keySet()).isPresent())) {
			Annotation annotation = firstAnnotation(method, events.keySet()).get();

			HandlerImpl handlerConfig = new HandlerImpl(
					new Filter[0],
					Invoke.Synchronously,
					0,
					false,
					true,
					RocketHandlerInvocation.class
			);

			MessageHandlerMetadata handler = new MessageHandlerMetadata(method, buildFilters(annotation), handlerConfig, metadata);
			setHandledMessages(handler, Arrays.asList(events.get(annotation.annotationType())));

			metadata.addHandler(handler);
		}

		return metadata;
	}

	private Optional<Annotation> firstAnnotation(AnnotatedElement elem, Iterable<Class<? extends Annotation>> annotations) {
		for (Class<? extends Annotation> annotation : annotations) {
			Annotation ann = elem.getAnnotation(annotation);
			if (ann != null) {
				return Optional.of(ann);
			}
		}
		return Optional.empty();
	}

	private void setHandledMessages(MessageHandlerMetadata handler, List<Class<?>> handledMessages) {
		try {
			Field field = handler.getClass().getDeclaredField("handledMessages");
			field.setAccessible(true);
			field.set(handler, handledMessages);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw propagate(e);
		}
	}

	private IMessageFilter[] buildFilters(Annotation annotation) {
		IMessageFilter filter = filters.get(annotation.annotationType());
		IMessageFilter[] filters;
		if (filter != null) {
			filters = new IMessageFilter[]{ filter };
		} else {
			filters = new IMessageFilter[0];
		}
		return filters;
	}

	private List<Method> findMethods(Class target, IPredicate<Method> condition) {
		List<Method> allHandlers = ReflectionUtils.getMethods(condition, target);
		List<Method> bottomMostHandlers = new LinkedList<>();
		for (Method handler : allHandlers) {
			if (!ReflectionUtils.containsOverridingMethod(allHandlers, handler)) {
				bottomMostHandlers.add(handler);
			}
		}
		return bottomMostHandlers;
	}
}
