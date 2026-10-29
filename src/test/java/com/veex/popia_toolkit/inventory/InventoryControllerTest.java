package com.veex.popia_toolkit.inventory;

import com.veex.popia_toolkit.auth.AppUserPrincipal;
import com.veex.popia_toolkit.auth.Role;
import com.veex.popia_toolkit.organisation.Organisation;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InventoryControllerTest
{
    private final InventoryService service = mock(InventoryService.class);
    private final InventoryController controller = new InventoryController(service);
    private final UUID organisationId = UUID.randomUUID();

    private AppUserPrincipal staffPrincipal()
    {
        return new AppUserPrincipal(UUID.randomUUID(), organisationId, "staff@example.com", "hash", Role.STAFF, true);
    }

    @Test void listShowsTheItemsOfTheLoggedInUsersOrganisation()
    {
        // Arrange
        InventoryItem item = new InventoryItem(new Organisation("Clinic A"), "Patient contacts",
                "Booking appointments", "Practice software", false);
        when(service.list(organisationId)).thenReturn(List.of(item));
        Model model = new ExtendedModelMap();

        // Act
        String view = controller.list(staffPrincipal(), model);

        // Assert
        assertEquals("inventory/list", view, "The list template should be shown");
        assertEquals(List.of(item), model.getAttribute("items"), "Only the organisation's items are passed on");
    }
}