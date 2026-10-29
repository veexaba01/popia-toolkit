package com.veex.popia_toolkit.auth;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AppUserPrincipal implements UserDetails
{
    private final UUID userId;
    private final UUID organisationId;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean enabled;

    public AppUserPrincipal(UUID userId, UUID organisationId, String email,
                            String passwordHash, Role role, boolean enabled)
    {
        this.userId = userId;
        this.organisationId = organisationId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    public UUID getUserId() { return userId; }
    public UUID getOrganisationId() { return organisationId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isEnabled() { return enabled; }
}