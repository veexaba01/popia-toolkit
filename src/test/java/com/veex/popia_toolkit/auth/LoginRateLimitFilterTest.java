package com.veex.popia_toolkit.auth;

import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LoginRateLimitFilterTest
{
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(2, Duration.ofMinutes(15), Clock.systemUTC());
    private final LoginRateLimitFilter filter = new LoginRateLimitFilter(tracker);

    private MockHttpServletRequest request(String method, String path, String username)
    {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setParameter("username", username);
        return request;
    }

    private void block(String username)
    {
        tracker.recordFailure(username);
        tracker.recordFailure(username);
    }

    @Test void passesALoginAttemptThroughWhileUnderTheLimit() throws Exception
    {
        // Arrange
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request("POST", "/login", "owner@example.com"), new MockHttpServletResponse(), chain);

        // Assert
        assertNotNull(chain.getRequest(), "The request should continue to the login handler");
    }

    @Test void refusesABlockedUsernameWithTheSameRedirectAsAFailedLogin() throws Exception
    {
        // Arrange
        block("owner@example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request("POST", "/login", "owner@example.com"), response, chain);

        // Assert
        assertEquals("/login?error", response.getRedirectedUrl(), "A block must look like any failed login");
        assertNull(chain.getRequest(), "A blocked attempt must not reach the password check");
    }

    @Test void doesNotAffectOtherPages() throws Exception
    {
        // Arrange
        block("owner@example.com");
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request("POST", "/register", "owner@example.com"), new MockHttpServletResponse(), chain);

        // Assert
        assertNotNull(chain.getRequest(), "Only the login post is limited");
    }

    @Test void doesNotBlockPageViewsOfTheLoginForm() throws Exception
    {
        // Arrange
        block("owner@example.com");
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilter(request("GET", "/login", "owner@example.com"), new MockHttpServletResponse(), chain);

        // Assert
        assertNotNull(chain.getRequest(), "Viewing the login page should always work");
    }
}