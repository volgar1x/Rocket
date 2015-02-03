package org.rocket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * A Stopwatch times a delimited block.
 *
 * <code>
 *     Stopwatch sw = new Stopwatch();
 *     try (Stopwatch.Handle h = sw.start()) {
 *         runTheMarathon();
 *     }
 *     printf("Congratulations, you ran the marathon in %d hours\n",
 *              sw.elapsed().toHours());
 * </code>
 */
public class Stopwatch {
    public class Handle implements AutoCloseable {
        public Handle() {
            start = Instant.now(clock);
        }

        /**
         * Stop the stopwatch.
         */
        public void stop() {
            close();
        }

        /**
         * Stop the stopwatch. Really useful when usd with a try-with-resource.
         */
        @Override
        public void close() {
            end = Instant.now(clock);
        }
    }

    private final Clock clock;
    private Instant start, end;

    /**
     * Construct a Stopwatch with a custom clock.
     * @param clock a custom clock
     */
    public Stopwatch(@Nullable Clock clock) {
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    /**
     * Construct a Stopwatch with {@link java.time.Clock#systemUTC()}.
     */
    public Stopwatch() {
        this(null);
    }

    /**
     * Start the stopwatch and return a handle used to stop the stopwatch.
     * @return
     * @see org.rocket.Stopwatch.Handle#stop()
     */
    public Handle start() {
        return new Handle();
    }

    /**
     * Give the elapsed time taken to execute a block of code.
     * @return a positive operation, very likely to not be zero (or you'll have to teach me how)
     */
    public Duration elapsed() {
        return Duration.between(start, end);
    }
}
