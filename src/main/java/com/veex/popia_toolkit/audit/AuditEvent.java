package com.veex.popia_toolkit.audit;

import com.veex.popia_toolkit.organisation.Organisation;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false, updatable = false)
    private Organisation organisation;

    @Column(name = "actor_email", nullable = false, updatable = false, length = 254)
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 50)
    private AuditAction action;

    @Column(updatable = false, length = 500)
    private String detail;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected AuditEvent() {}

    public AuditEvent(Organisation organisation, String actorEmail, AuditAction action, String detail)
    {
        this(organisation, actorEmail, action, detail, Instant.now());
    }

    public AuditEvent(Organisation organisation, String actorEmail, AuditAction action,
                      String detail, Instant occurredAt)
    {
        this.organisation = organisation;
        this.actorEmail = actorEmail;
        this.action = action;
        this.detail = detail;
        this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public String getActorEmail() { return actorEmail; }
    public AuditAction getAction() { return action; }
    public String getDetail() { return detail; }
    public Instant getOccurredAt() { return occurredAt; }
}