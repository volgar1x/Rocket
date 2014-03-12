package org.rocket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class Stopwatch {
    public class Handle implements AutoCloseable {
        public Handle() {
            start = Instant.now(clock);
        }

        @Override
        public void close() {
            end = Instant.now(clock);
        }
    }

    private final Clock clock;
    private Instant start, end;

    public Stopwatch(Clock clock) {
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    public Stopwatch() {
        this(null);
    }

    public Handle start() {
        return new Handle();
    }

    public Duration elapsed() {
        return Duration.between(start, end);
    }
}
