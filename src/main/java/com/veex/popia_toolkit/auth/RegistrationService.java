package com.veex.popia_toolkit.auth;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService
{
    private final OrganisationRepository organisations;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(OrganisationRepository organisations,
                               AppUserRepository users,
                               PasswordEncoder passwordEncoder)
    {
        this.organisations = organisations;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegistrationRequest request)
    {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String organisationName = request.organisationName().trim();

        if (users.findByEmailIgnoreCase(email).isPresent())
        {
            throw new EmailAlreadyRegisteredException();
        }

        Organisation organisation = organisations.save(new Organisation(organisationName));
        String passwordHash = passwordEncoder.encode(request.password());

        return users.save(new AppUser(organisation, email, passwordHash, Role.ADMIN));
    }
}