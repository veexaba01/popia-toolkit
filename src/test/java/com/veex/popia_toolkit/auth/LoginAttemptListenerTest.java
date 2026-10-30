package com.veex.popia_toolkit.auth;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LoginAttemptListenerTest
{
    private final LoginAttemptTracker tracker = mock(LoginAttemptTracker.class);
    private final LoginAttemptListener listener = new LoginAttemptListener(tracker);

    @Test void recordsAFailureForTheSubmittedUsername()
    {
        // Arrange
        AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                new UsernamePasswordAuthenticationToken("nobody@example.com", "wrong"),
                new BadCredentialsException("Bad credentials"));

        // Act
        listener.onFailure(event);

        // Assert
        verify(tracker).recordFailure("nobody@example.com");
    }

    @Test void clearsTheHistoryAfterASuccessfulLogin()
    {
        // Arrange
        AppUserPrincipal principal = new AppUserPrincipal(UUID.randomUUID(), UUID.randomUUID(),
                "owner@example.com", "hash", Role.ADMIN, true);
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        // Act
        listener.onSuccess(event);

        // Assert
        verify(tracker).recordSuccess("owner@example.com");
    }
}