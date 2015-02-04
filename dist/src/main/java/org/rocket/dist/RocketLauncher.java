package org.rocket.dist;

import com.google.inject.*;
import com.google.inject.spi.Message;
import org.rocket.Service;
import org.rocket.ServiceGraph;
import org.rocket.Services;
import org.rocket.StartReason;

import java.util.Collection;
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
            run(rocket);
        } catch (CreationException e) {
            printGuiceMessages(e.getErrorMessages());
        } catch (ProvisionException e) {
            printGuiceMessages(e.getErrorMessages());
        }
    }

    /**
     * Run a {@link org.rocket.dist.Rocket}
     * @param rocket a non-null rocket
     */
    @Deprecated
    public static void run(Rocket rocket) {
        requireNonNull(rocket, "rocket");

        ServiceGraph services = findBindings(rocket.createInjector(), Key.get(Service.class))
                .<Service>map(x -> x.getProvider().get())
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        Services::newGraph));

        // fold services
        Service folded = services.fold();

        // go live!
        folded.start(StartReason.NORMAL);
        Runtime.getRuntime().addShutdownHook(new Thread(folded::stop));
    }

    private static void printGuiceMessages(Collection<Message> messages) {
        for (Message message : messages) {
            System.err.println(message.getMessage());

            for (Object source : message.getSources()) {
                System.err.println("  -> " + source);
            }

            Throwable cause = message.getCause();
            if (cause != null) {
                cause.printStackTrace(System.err);
            }
        }
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
