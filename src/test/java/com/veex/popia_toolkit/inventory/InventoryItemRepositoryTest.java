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

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        @Test void listsOnlyTheItemsOfTheGivenOrganisationInNameOrder()
    {
        // Arrange
        items.save(new InventoryItem(clinicA, "Staff records", "Payroll", "Accounting system", false));
        items.save(new InventoryItem(clinicA, "Patient contacts", "Booking appointments", "Practice software", false));
        items.save(new InventoryItem(clinicB, "Learner records", "Admissions", "Filing cabinet", false));

        // Act
        List<InventoryItem> result = items.findAllByOrganisationIdOrderByNameAsc(clinicA.getId());

        // Assert
        assertEquals(2, result.size(), "Only clinic A's items should be listed");
        assertEquals("Patient contacts", result.get(0).getName(), "Items are sorted by name");
        assertEquals("Staff records", result.get(1).getName());
    }

    @Test void findsAnItemThroughItsOwnOrganisation()
    {
        // Arrange
        InventoryItem item = items.save(new InventoryItem(clinicA, "Patient contacts", "Booking", "Software", false));

        // Act
        Optional<InventoryItem> result = items.findByIdAndOrganisationId(item.getId(), clinicA.getId());

        // Assert
        assertTrue(result.isPresent(), "The owning organisation should find its own item");
    }

    @Test void doesNotFindAnItemThroughAnotherOrganisation()
    {
        // Arrange
        InventoryItem item = items.save(new InventoryItem(clinicA, "Patient contacts", "Booking", "Software", false));

        // Act
        Optional<InventoryItem> result = items.findByIdAndOrganisationId(item.getId(), clinicB.getId());

        // Assert
        assertTrue(result.isEmpty(), "Clinic B must never reach clinic A's record, even with the right id");
    }
}