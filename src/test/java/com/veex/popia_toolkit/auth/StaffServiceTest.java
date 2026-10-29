package com.veex.popia_toolkit.auth;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class StaffServiceTest
{
    private static final String PASSWORD = "a-long-enough-password";

    @Autowired
    private StaffService staffService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test void createsStaffInTheSameOrganisation()
    {
        // Arrange
        AppUser admin = registrationService.register(
                new RegistrationRequest("Test Clinic", "owner@example.com", PASSWORD));
        UUID organisationId = admin.getOrganisation().getId();

        // Act
        AppUser staff = staffService.createStaff(organisationId, "staff@example.com", PASSWORD);

        // Assert
        assertEquals(Role.STAFF, staff.getRole(), "New members should be staff, never admins");
        assertEquals(organisationId, staff.getOrganisation().getId(), "Staff belong to the admin's organisation");
        assertNotEquals(PASSWORD, staff.getPasswordHash(), "The raw password must never be stored");
        assertTrue(passwordEncoder.matches(PASSWORD, staff.getPasswordHash()), "The stored hash should match");
    }

        @Test void normalisesStaffEmailToLowercase()
    {
        // Arrange
        AppUser admin = registrationService.register(
                new RegistrationRequest("Test Clinic", "owner@example.com", PASSWORD));

        // Act
        AppUser staff = staffService.createStaff(admin.getOrganisation().getId(), "  Staff@Example.COM ", PASSWORD);

        // Assert
        assertEquals("staff@example.com", staff.getEmail(), "Emails should be stored trimmed and in lowercase");
    }

    @Test void rejectsAnEmailThatIsAlreadyRegisteredInAnyOrganisation()
    {
        // Arrange
        registrationService.register(new RegistrationRequest("Clinic One", "owner@example.com", PASSWORD));
        AppUser otherAdmin = registrationService.register(
                new RegistrationRequest("Clinic Two", "other@example.com", PASSWORD));

        // Act and Assert
        assertThrows(EmailAlreadyRegisteredException.class,
                () -> staffService.createStaff(otherAdmin.getOrganisation().getId(), "OWNER@example.com", PASSWORD),
                "An email can belong to only one account across the whole system");
    }
}