package com.veex.popia_toolkit.auth;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;

import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffService
{
    private final OrganisationRepository organisations;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public StaffService(OrganisationRepository organisations,
                        AppUserRepository users,
                        PasswordEncoder passwordEncoder)
    {
        this.organisations = organisations;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

        @Transactional
    public AppUser createStaff(UUID organisationId, String email, String rawPassword)
    {
        String normalisedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (users.findByEmailIgnoreCase(normalisedEmail).isPresent())
        {
            throw new EmailAlreadyRegisteredException();
        }

        Organisation organisation = organisations.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));
        String passwordHash = passwordEncoder.encode(rawPassword);

        return users.save(new AppUser(organisation, normalisedEmail, passwordHash, Role.STAFF));
    }
}