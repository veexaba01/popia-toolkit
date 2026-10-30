package com.veex.popia_toolkit.auth;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptTracker
{
    private final int maxFailures;
    private final Map<String, Integer> failures = new HashMap<>();

    public LoginAttemptTracker()
    {
        this(5);
    }

    public LoginAttemptTracker(int maxFailures)
    {
        this.maxFailures = maxFailures;
    }

    public synchronized void recordFailure(String username)
    {
        failures.merge(key(username), 1, Integer::sum);
    }

    public synchronized boolean isBlocked(String username)
    {
        return failures.getOrDefault(key(username), 0) >= maxFailures;
    }

    private String key(String username)
    {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}