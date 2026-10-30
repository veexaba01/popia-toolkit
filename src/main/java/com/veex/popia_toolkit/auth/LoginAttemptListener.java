package com.veex.popia_toolkit.auth;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptListener
{
    private final LoginAttemptTracker tracker;

    public LoginAttemptListener(LoginAttemptTracker tracker)
    {
        this.tracker = tracker;
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event)
    {
        tracker.recordFailure(event.getAuthentication().getName());
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event)
    {
        tracker.recordSuccess(event.getAuthentication().getName());
    }
}