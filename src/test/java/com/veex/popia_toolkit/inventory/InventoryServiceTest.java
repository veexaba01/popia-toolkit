package com.veex.popia_toolkit.inventory;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import java.util.UUID;
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

    private InventoryItem getAsClinicA(InventoryItem item)
    {
        return service.get(clinicA.getId(), item.getId());
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

    @Test void listsOnlyTheItemsOfTheGivenOrganisation()
    {
        // Arrange
        service.create(clinicA.getId(), details("Staff records"));
        service.create(clinicA.getId(), details("Patient contacts"));
        service.create(clinicB.getId(), details("Learner records"));

        // Act
        var result = service.list(clinicA.getId());

        // Assert
        assertEquals(2, result.size(), "Only clinic A's items should be listed");
        assertEquals("Patient contacts", result.get(0).getName(), "Items are sorted by name");
    }

    @Test void getsAnItemOfTheSameOrganisation()
    {
        // Arrange
        InventoryItem created = service.create(clinicA.getId(), details("Patient contacts"));

        // Act
        InventoryItem found = getAsClinicA(created);

        // Assert
        assertEquals(created.getId(), found.getId());
    }

    @Test void cannotGetAnItemOfAnotherOrganisation()
    {
        // Arrange
        InventoryItem created = service.create(clinicA.getId(), details("Patient contacts"));

        // Act and Assert
        assertThrows(InventoryItemNotFoundException.class,
                () -> service.get(clinicB.getId(), created.getId()),
                "Clinic B must not be able to read clinic A's item, even with the right id");
    }

    @Test void reportsNotFoundForAnUnknownId()
    {
        // Act and Assert
        assertThrows(InventoryItemNotFoundException.class,
                () -> service.get(clinicA.getId(), UUID.randomUUID()),
                "An unknown id and another organisation's id should look identical");
    }

    @Test void updatesAnItemOfTheSameOrganisation()
    {
        // Arrange
        InventoryItem created = service.create(clinicA.getId(), details("Patient contacts"));
        InventoryItemDetails changes = new InventoryItemDetails("Patient records", "Treatment", "Locked cabinet", true);

        // Act
        InventoryItem updated = service.update(clinicA.getId(), created.getId(), changes);

        // Assert
        assertEquals("Patient records", updated.getName());
        assertEquals("Treatment", updated.getPurpose());
        assertEquals("Locked cabinet", updated.getStorageLocation());
        assertTrue(updated.isSpecialInformation());
    }

    @Test void cannotUpdateAnItemOfAnotherOrganisation()
    {
        // Arrange
        InventoryItem created = service.create(clinicA.getId(), details("Patient contacts"));
        InventoryItemDetails changes = new InventoryItemDetails("Hacked", "Hacked", "Hacked", true);

        // Act and Assert
        assertThrows(InventoryItemNotFoundException.class,
                () -> service.update(clinicB.getId(), created.getId(), changes),
                "Clinic B must not be able to change clinic A's item");
        assertEquals("Patient contacts", getAsClinicA(created).getName(),
                "The original item must be unchanged");
    }

    @Test void deletesAnItemOfTheSameOrganisation()
    {
        // Arrange
        InventoryItem created = service.create(clinicA.getId(), details("Patient contacts"));

        // Act
        service.delete(clinicA.getId(), created.getId());

        // Assert
        assertThrows(InventoryItemNotFoundException.class,
                () -> getAsClinicA(created),
                "The item should be gone after deletion");
    }

    @Test void cannotDeleteAnItemOfAnotherOrganisation()
    {
        // Arrange
        InventoryItem created = service.create(clinicA.getId(), details("Patient contacts"));

        // Act and Assert
        assertThrows(InventoryItemNotFoundException.class,
                () -> service.delete(clinicB.getId(), created.getId()),
                "Clinic B must not be able to delete clinic A's item");
        assertEquals(created.getId(), getAsClinicA(created).getId(),
                "The item must still exist for its owner");
    }
}