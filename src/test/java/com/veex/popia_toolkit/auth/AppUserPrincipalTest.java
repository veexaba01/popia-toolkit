package com.veex.popia_toolkit.auth;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppUserPrincipalTest
{
    private final UUID userId = UUID.randomUUID();
    private final UUID organisationId = UUID.randomUUID();

    private AppUserPrincipal principal(Role role, boolean enabled)
    {
        return new AppUserPrincipal(userId, organisationId, "owner@example.com", "stored-hash", role, enabled);
    }

    @Test void usesEmailAsUsernameAndHashAsPassword()
    {
        // Arrange and Act
        AppUserPrincipal principal = principal(Role.ADMIN, true);

        // Assert
        assertEquals("owner@example.com", principal.getUsername(), "The email is the login name");
        assertEquals("stored-hash", principal.getPassword(), "Spring compares against the stored hash");
    }

    @Test void grantsAnAuthorityMatchingTheRole()
    {
        // Arrange and Act
        AppUserPrincipal principal = principal(Role.STAFF, true);

        // Assert
        assertTrue(principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")),
                "A staff user should have the ROLE_STAFF authority");
        assertEquals(1, principal.getAuthorities().size(), "A user has exactly one role");
    }

    @Test void exposesOrganisationAndUserIds()
    {
        // Arrange and Act
        AppUserPrincipal principal = principal(Role.ADMIN, true);

        // Assert
        assertEquals(organisationId, principal.getOrganisationId(), "Needed later to scope data to the organisation");
        assertEquals(userId, principal.getUserId());
    }

    @Test void reflectsTheEnabledFlag()
    {
        // Arrange and Act
        AppUserPrincipal disabled = principal(Role.ADMIN, false);

        // Assert
        assertFalse(disabled.isEnabled(), "A disabled account must not be able to log in");
    }
}