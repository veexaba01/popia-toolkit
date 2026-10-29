package com.veex.popia_toolkit.auth;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class AppUserDetailsServiceTest
{
    @Autowired
    private AppUserDetailsService detailsService;

    @Autowired
    private OrganisationRepository organisations;

    @Autowired
    private AppUserRepository users;

    @Test void loadsUserByEmailIgnoringCase()
    {
        // Arrange
        Organisation org = organisations.save(new Organisation("Test Clinic"));
        users.save(new AppUser(org, "staff@example.com", "stored-hash", Role.STAFF));

        // Act
        UserDetails details = detailsService.loadUserByUsername("STAFF@example.com");

        // Assert
        assertEquals("staff@example.com", details.getUsername());
        assertEquals("stored-hash", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")));
    }

    @Test void exposesTheOrganisationIdOnThePrincipal()
    {
        // Arrange
        Organisation org = organisations.save(new Organisation("Test Clinic"));
        users.save(new AppUser(org, "staff@example.com", "stored-hash", Role.STAFF));

        // Act
        AppUserPrincipal principal = (AppUserPrincipal) detailsService.loadUserByUsername("staff@example.com");

        // Assert
        assertEquals(org.getId(), principal.getOrganisationId(), "The principal must know which organisation it belongs to");
    }

    @Test void throwsWhenTheUserDoesNotExist()
    {
        // Act and Assert
        assertThrows(UsernameNotFoundException.class,
                () -> detailsService.loadUserByUsername("nobody@example.com"),
                "An unknown email must not produce a user");
    }
}