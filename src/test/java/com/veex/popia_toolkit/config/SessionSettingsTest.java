package com.veex.popia_toolkit.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SessionSettingsTest
{
    @Autowired
    private Environment environment;

    @Test void sessionCookieIsNotReadableByScripts()
    {
        assertEquals("true", environment.getProperty("server.servlet.session.cookie.http-only"),
                "HttpOnly stops JavaScript from reading the session cookie");
    }

    @Test void sessionCookieIsNotSentOnCrossSiteRequests()
    {
        assertEquals("lax", environment.getProperty("server.servlet.session.cookie.same-site"),
                "SameSite limits the cookie to requests that start on this site");
    }

    @Test void idleSessionsExpireAfterThirtyMinutes()
    {
        assertEquals("30m", environment.getProperty("server.servlet.session.timeout"),
                "A shorter lifetime limits how long a stolen session is useful");
    }
}