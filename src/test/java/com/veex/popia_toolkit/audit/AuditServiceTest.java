package com.veex.popia_toolkit.audit;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class AuditServiceTest
{
    @Autowired
    private AuditService service;

    @Autowired
    private OrganisationRepository organisations;

    private Organisation clinicA;
    private Organisation clinicB;

    @BeforeEach
    void setUp()
    {
        clinicA = organisations.save(new Organisation("Clinic A"));
        clinicB = organisations.save(new Organisation("Clinic B"));
    }

    @Test void recordsAnEventForTheOrganisation()
    {
        // Act
        service.record(clinicA.getId(), "owner@example.com", AuditAction.INVENTORY_CREATED, "Item: Contacts");

        // Assert
        List<AuditEvent> result = service.recent(clinicA.getId());
        assertEquals(1, result.size());
        assertEquals("owner@example.com", result.get(0).getActorEmail());
        assertEquals(AuditAction.INVENTORY_CREATED, result.get(0).getAction());
        assertEquals("Item: Contacts", result.get(0).getDetail());
    }

    @Test void recentShowsOnlyTheEventsOfTheGivenOrganisation()
    {
        // Arrange
        service.record(clinicA.getId(), "a@example.com", AuditAction.LOGIN_SUCCESS, null);
        service.record(clinicB.getId(), "b@example.com", AuditAction.LOGIN_SUCCESS, null);

        // Act
        List<AuditEvent> result = service.recent(clinicA.getId());

        // Assert
        assertEquals(1, result.size(), "Clinic A must not see clinic B's events");
        assertEquals("a@example.com", result.get(0).getActorEmail());
    }

    @Test void refusesToRecordForAnUnknownOrganisation()
    {
        // Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.record(java.util.UUID.randomUUID(), "a@example.com", AuditAction.LOGIN_SUCCESS, null),
                "An event must always belong to a real organisation");
    }

    @Test void recordsAPasswordChangeEvent()
    {
        // Act
        service.record(clinicA.getId(), "staff@example.com", AuditAction.PASSWORD_CHANGED, null);

        // Assert
        assertEquals(AuditAction.PASSWORD_CHANGED, service.recent(clinicA.getId()).get(0).getAction());
    }
}