package com.veex.popia_toolkit.auth;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Organisation organisation = organisations.save(new Organisation(request.organisationName()));
        String passwordHash = passwordEncoder.encode(request.password());

        return users.save(new AppUser(organisation, request.email(), passwordHash, Role.ADMIN));
    }
}