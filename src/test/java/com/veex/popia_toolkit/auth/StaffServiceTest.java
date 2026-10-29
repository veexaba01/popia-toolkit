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
}