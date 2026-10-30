package com.veex.popia_toolkit.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginAttemptTrackerTest
{
    private static class MutableClock extends Clock
    {
        private Instant now = Instant.parse("2026-10-30T08:00:00Z");

        void advance(Duration amount)
        {
            now = now.plus(amount);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    private final MutableClock clock = new MutableClock();
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(3, Duration.ofMinutes(15), clock);

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

    @Test void staysBlockedInsideTheWindow()
    {
        // Arrange
        fail("owner@example.com", 3);

        // Act
        clock.advance(Duration.ofMinutes(10));

        // Assert
        assertTrue(tracker.isBlocked("owner@example.com"), "Ten minutes later the block should still apply");
    }

    @Test void blockEndsOnceTheFailuresAreOlderThanTheWindow()
    {
        // Arrange
        fail("owner@example.com", 3);

        // Act
        clock.advance(Duration.ofMinutes(15).plusSeconds(1));

        // Assert
        assertFalse(tracker.isBlocked("owner@example.com"), "After the window passes the user can try again");
    }

    @Test void oldFailuresDoNotCountTowardsTheLimit()
    {
        // Arrange
        fail("owner@example.com", 2);
        clock.advance(Duration.ofMinutes(16));

        // Act
        fail("owner@example.com", 1);

        // Assert
        assertFalse(tracker.isBlocked("owner@example.com"), "Only one failure is recent, which is under the limit");
    }

    @Test void aSuccessfulLoginClearsEarlierFailures()
    {
        // Arrange
        fail("owner@example.com", 2);

        // Act
        tracker.recordSuccess("owner@example.com");
        fail("owner@example.com", 1);

        // Assert
        assertFalse(tracker.isBlocked("owner@example.com"), "Failures before a success should not count");
    }
}