package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginAttemptTrackerTest
{
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(3);

    private void fail(String username, int times)
    {
        for (int i = 0; i < times; i++)
        {
            tracker.recordFailure(username);
        }
    }

    @Test void isNotBlockedBeforeAnyFailure()
    {
        assertFalse(tracker.isBlocked("owner@example.com"), "A fresh username should not be blocked");
    }

    @Test void isNotBlockedBelowTheLimit()
    {
        // Act
        fail("owner@example.com", 2);

        // Assert
        assertFalse(tracker.isBlocked("owner@example.com"), "Two failures are under the limit of three");
    }

    @Test void isBlockedOnceTheLimitIsReached()
    {
        // Act
        fail("owner@example.com", 3);

        // Assert
        assertTrue(tracker.isBlocked("owner@example.com"), "Three failures should block the username");
    }

    @Test void tracksEachUsernameSeparately()
    {
        // Act
        fail("owner@example.com", 3);

        // Assert
        assertFalse(tracker.isBlocked("staff@example.com"), "Another username must not be affected");
    }

    @Test void ignoresCaseAndSurroundingSpaces()
    {
        // Act
        fail("Owner@Example.com", 3);

        // Assert
        assertTrue(tracker.isBlocked("  owner@example.com "), "Variations of the same email count as one");
    }
}