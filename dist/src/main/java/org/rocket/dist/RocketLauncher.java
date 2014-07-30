package org.rocket.dist;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.internal.util.$ComputationException;
import org.fungsi.Throwables;
import org.rocket.Service;
import org.rocket.ServiceContext;
import org.rocket.Services;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class RocketLauncher {
    private RocketLauncher() {}

    /**
     * Take off your {@link org.rocket.dist.Rocket}
     * @param rocket a non-null rocket
     */
    @SuppressWarnings("deprecation")
    public static void takeOff(Rocket rocket) {
        requireNonNull(rocket, "rocket");

        try {
            run(rocket.getServiceContext());
        } catch ($ComputationException e) {
            throw Throwables.propagate(getRootCause(e));
        }
    }

    /**
     * Run a {@link org.rocket.ServiceContext}
     * @param ctx a non-null service context
     */
    @Deprecated
    public static void run(ServiceContext ctx) {
        requireNonNull(ctx, "ctx");

        Services.Graph services = findBindings(ctx.getInjector(), Key.get(Service.class))
                .<Service>map(x -> x.getProvider().get())
                .collect(Collectors.collectingAndThen(
                        Collectors.toSet(),
                        Services::createGraph));

        // fold services
        Service folded = services.fold();

        // go live!
        folded.start(ctx);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> folded.stop(ctx)));
    }

    private static Throwable getRootCause(Throwable t) {
        if (t.getCause() == null) {
            return t;
        }
        return t.getCause();
    }

    private static boolean isExtending(Key<?> parent, Key<?> child) {
        return parent.getTypeLiteral().getRawType().isAssignableFrom(child.getTypeLiteral().getRawType());
    }

    @SuppressWarnings("unchecked")
    private static <T> Stream<Binding<? extends T>> findBindings(Injector injector, Key<T> tpe) {
        return injector.getAllBindings().values().stream()
                .filter(x -> isExtending(tpe, x.getKey()))
                .map(x -> (Binding<? extends T>) x)
                ;
    }
}
