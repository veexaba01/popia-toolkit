package com.veex.popia_toolkit.inventory;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Transactional
public class InventoryItemRepositoryTest
{
    @Autowired
    private OrganisationRepository organisations;

    @Autowired
    private InventoryItemRepository items;

    private Organisation clinicA;
    private Organisation clinicB;

    @BeforeEach
    void setUp()
    {
        clinicA = organisations.save(new Organisation("Clinic A"));
        clinicB = organisations.save(new Organisation("Clinic B"));
    }

    @Test void savesAnItemWithItsDetails()
    {
        // Arrange
        InventoryItem item = new InventoryItem(clinicA, "Patient contacts", "Booking appointments",
                "Practice software", false);

        // Act
        InventoryItem saved = items.saveAndFlush(item);

        // Assert
        InventoryItem loaded = items.findById(saved.getId()).orElseThrow();
        assertEquals("Patient contacts", loaded.getName());
        assertEquals("Booking appointments", loaded.getPurpose());
        assertEquals("Practice software", loaded.getStorageLocation());
        assertFalse(loaded.isSpecialInformation(), "Special information is off unless stated");
        assertEquals(clinicA.getId(), loaded.getOrganisation().getId(), "The item belongs to its organisation");
    }
}