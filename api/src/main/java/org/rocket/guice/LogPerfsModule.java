package org.rocket.guice;

import com.google.inject.AbstractModule;
import org.rocket.LogPerfs;
import org.rocket.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static com.google.inject.matcher.Matchers.any;
import static org.rocket.guice.Matchers.matcher;

public final class LogPerfsModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(LogPerfsModule.class);

    private final ChronoUnit unit;

    public LogPerfsModule(ChronoUnit unit) {
        this.unit = Objects.requireNonNull(unit, "unit");
    }

    @Override
    protected void configure() {
        bindInterceptor(
                any(),
                matcher(m -> {
                    LogPerfs ann = m.getAnnotation(LogPerfs.class);
                    return ann != null && ann.value();
                }),
                invocation -> {
                    Stopwatch sw = new Stopwatch();

                    Object result = null;
                    try (Stopwatch.Handle ignored = sw.start()) {
                        result = invocation.proceed();
                    }

                    log.debug("{} took {} {} to complete",
                            invocation.getMethod().toGenericString(),
                            sw.elapsed().get(unit),
                            unit.toString());

                    return result;
                }
        );
    }
}
