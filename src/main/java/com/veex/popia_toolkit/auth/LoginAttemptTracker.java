package com.veex.popia_toolkit.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptTracker
{
    private final int maxFailures;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> failures = new HashMap<>();

    public LoginAttemptTracker()
    {
        this(5, Duration.ofMinutes(15), Clock.systemUTC());
    }

    public LoginAttemptTracker(int maxFailures, Duration window, Clock clock)
    {
        this.maxFailures = maxFailures;
        this.window = window;
        this.clock = clock;
    }

    public synchronized void recordFailure(String username)
    {
        Instant now = clock.instant();
        removeAllExpired(now);
        failures.computeIfAbsent(key(username), k -> new ArrayDeque<>()).addLast(now);
    }

    public synchronized void recordSuccess(String username)
    {
        failures.remove(key(username));
    }

    public synchronized boolean isBlocked(String username)
    {
        Deque<Instant> recent = failures.get(key(username));

        if (recent == null)
        {
            return false;
        }

        removeExpired(recent, clock.instant());
        return recent.size() >= maxFailures;
    }

    private void removeAllExpired(Instant now)
    {
        failures.values().removeIf(recent ->
        {
            removeExpired(recent, now);
            return recent.isEmpty();
        });
    }

    private void removeExpired(Deque<Instant> recent, Instant now)
    {
        Instant cutoff = now.minus(window);

        while (!recent.isEmpty() && !recent.peekFirst().isAfter(cutoff))
        {
            recent.removeFirst();
        }
    }

    private String key(String username)
    {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}