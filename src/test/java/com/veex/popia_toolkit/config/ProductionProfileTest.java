package com.veex.popia_toolkit.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductionProfileTest
{
    @Test void productionSessionCookieIsOnlySentOverHttps() throws IOException
    {
        // Arrange
        Properties properties = new Properties();

        // Act
        try (InputStream input = new ClassPathResource("application-prod.properties").getInputStream())
        {
            properties.load(input);
        }

        // Assert
        assertEquals("true", properties.getProperty("server.servlet.session.cookie.secure"),
                "In production the session cookie must carry the Secure flag");
    }
}