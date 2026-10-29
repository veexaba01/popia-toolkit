package com.veex.popia_toolkit.inventory;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class InventoryServiceTest
{
    @Autowired
    private InventoryService service;

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

    private InventoryItemDetails details(String name)
    {
        return new InventoryItemDetails(name, "Booking appointments", "Practice software", false);
    }

    @Test void createsAnItemInTheGivenOrganisation()
    {
        // Act
        InventoryItem item = service.create(clinicA.getId(), details("Patient contacts"));

        // Assert
        assertEquals(clinicA.getId(), item.getOrganisation().getId(), "The item belongs to the organisation given");
        assertEquals("Patient contacts", item.getName());
        assertEquals("Booking appointments", item.getPurpose());
        assertEquals("Practice software", item.getStorageLocation());
        assertFalse(item.isSpecialInformation());
    }
}