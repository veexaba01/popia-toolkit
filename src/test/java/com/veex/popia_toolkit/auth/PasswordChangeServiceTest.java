package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class PasswordChangeServiceTest
{
    private static final String OLD_PASSWORD = "old-password-1234";
    private static final String NEW_PASSWORD = "new-password-5678";

    @Autowired
    private PasswordChangeService service;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AppUser registerUser()
    {
        return registrationService.register(new RegistrationRequest("Test Clinic", "owner@example.com", OLD_PASSWORD));
    }

    @Test void changesThePasswordWhenTheCurrentOneIsCorrect()
    {
        // Arrange
        AppUser user = registerUser();

        // Act
        service.changePassword(user.getId(), OLD_PASSWORD, NEW_PASSWORD);

        // Assert
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, user.getPasswordHash()), "The new password should work");
        assertFalse(passwordEncoder.matches(OLD_PASSWORD, user.getPasswordHash()), "The old password should stop working");
        assertNotEquals(NEW_PASSWORD, user.getPasswordHash(), "Only a hash is stored");
    }

    @Test void refusesAWrongCurrentPasswordAndKeepsTheOldOne()
    {
        // Arrange
        AppUser user = registerUser();

        // Act and Assert
        assertThrows(InvalidCurrentPasswordException.class,
                () -> service.changePassword(user.getId(), "not-the-password-1", NEW_PASSWORD),
                "A wrong current password must stop the change");
        assertTrue(passwordEncoder.matches(OLD_PASSWORD, user.getPasswordHash()), "The password must be unchanged");
    }
}