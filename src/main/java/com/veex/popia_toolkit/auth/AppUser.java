package com.veex.popia_toolkit.auth;


import com.veex.popia_toolkit.organisation.Organisation;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class AppUser
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AppUser() {}

    public AppUser(Organisation organisation, String email, String passwordHash, Role role)
    {
        this.organisation = organisation;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public UUID getId() {return id;}
    public Organisation getOrganisation() {return organisation;}
    public String getEmail() {return email;}
    public String getPasswordHash() {return passwordHash;}
    public Role getRole() {return role;}
    public boolean isEnabled() {return enabled;}

}
