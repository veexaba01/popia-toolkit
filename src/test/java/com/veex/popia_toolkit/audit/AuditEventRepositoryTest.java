package com.veex.popia_toolkit.audit;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class AuditEventRepositoryTest
{
    @Autowired
    private OrganisationRepository organisations;

    @Autowired
    private AuditEventRepository events;

    private Organisation clinicA;
    private Organisation clinicB;

    @BeforeEach
    void setUp()
    {
        clinicA = organisations.save(new Organisation("Clinic A"));
        clinicB = organisations.save(new Organisation("Clinic B"));
    }

    @Test void savesAnEventWithItsDetails()
    {
        // Arrange
        AuditEvent event = new AuditEvent(clinicA, "owner@example.com", AuditAction.STAFF_CREATED, "Staff: a@example.com");

        // Act
        AuditEvent saved = events.saveAndFlush(event);

        // Assert
        AuditEvent loaded = events.findById(saved.getId()).orElseThrow();
        assertEquals("owner@example.com", loaded.getActorEmail());
        assertEquals(AuditAction.STAFF_CREATED, loaded.getAction());
        assertEquals("Staff: a@example.com", loaded.getDetail());
        assertEquals(clinicA.getId(), loaded.getOrganisation().getId());
    }

    @Test void listsOnlyTheEventsOfTheGivenOrganisationNewestFirst()
    {
        // Arrange
        events.save(new AuditEvent(clinicA, "a@example.com", AuditAction.LOGIN_SUCCESS, null,
                Instant.parse("2026-10-01T08:00:00Z")));
        events.save(new AuditEvent(clinicA, "a@example.com", AuditAction.INVENTORY_CREATED, "Item: Contacts",
                Instant.parse("2026-10-01T09:00:00Z")));
        events.save(new AuditEvent(clinicB, "b@example.com", AuditAction.LOGIN_SUCCESS, null,
                Instant.parse("2026-10-01T10:00:00Z")));

        // Act
        List<AuditEvent> result = events.findTop100ByOrganisationIdOrderByOccurredAtDesc(clinicA.getId());

        // Assert
        assertEquals(2, result.size(), "Clinic B's event must never appear for clinic A");
        assertEquals(AuditAction.INVENTORY_CREATED, result.get(0).getAction(), "The newest event comes first");
        assertEquals(AuditAction.LOGIN_SUCCESS, result.get(1).getAction());
    }
}