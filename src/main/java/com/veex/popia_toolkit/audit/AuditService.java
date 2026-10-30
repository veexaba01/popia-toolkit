package com.veex.popia_toolkit.audit;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService
{
    private final AuditEventRepository events;
    private final OrganisationRepository organisations;

    public AuditService(AuditEventRepository events, OrganisationRepository organisations)
    {
        this.events = events;
        this.organisations = organisations;
    }

    @Transactional
    public void record(UUID organisationId, String actorEmail, AuditAction action, String detail)
    {
        Organisation organisation = organisations.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        events.save(new AuditEvent(organisation, actorEmail, action, detail));
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> recent(UUID organisationId)
    {
        return events.findTop100ByOrganisationIdOrderByOccurredAtDesc(organisationId);
    }
}