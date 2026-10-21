package com.veex.popia_toolkit.organisation;


import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organisations")
public class Organisation
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Organisation() {}

    public Organisation(String name)
    {
        this.name = name;
    }

    public UUID getId() {return id;}
    public String getName() {return name;}
    public Instant getCreatedAt() {return createdAt;}
}
