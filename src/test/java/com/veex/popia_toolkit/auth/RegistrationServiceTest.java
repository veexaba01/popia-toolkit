package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class RegistrationServiceTest
{
    private static final String PASSWORD = "a-long-enough-password";

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test void createsAdminWithHashedPassword()
    {
        // Arrange
        RegistrationRequest request = new RegistrationRequest("Test Clinic", "owner@example.com", PASSWORD);

        // Act
        AppUser user = registrationService.register(request);

        // Assert
        assertEquals(Role.ADMIN, user.getRole(), "The first user of an organisation should be its admin");
        assertEquals("Test Clinic", user.getOrganisation().getName(), "The organisation should be created with the given name");
        assertNotEquals(PASSWORD, user.getPasswordHash(), "The raw password must never be stored");
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPasswordHash()), "The stored hash should match the password");
    }
}