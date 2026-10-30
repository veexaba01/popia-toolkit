package com.veex.popia_toolkit.inventory;

import com.veex.popia_toolkit.auth.AppUserPrincipal;
import com.veex.popia_toolkit.auth.Role;
import com.veex.popia_toolkit.organisation.Organisation;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    private InventoryItemForm validForm()
    {
        InventoryItemForm form = new InventoryItemForm();
        form.setName("Patient contacts");
        form.setPurpose("Booking appointments");
        form.setStorageLocation("Practice software");
        form.setSpecialInformation(false);
        return form;
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

    @Test void showNewFormReturnsTheFormViewWithAnEmptyForm()
    {
        // Arrange
        Model model = new ExtendedModelMap();

        // Act
        String view = controller.showNewForm(model);

        // Assert
        assertEquals("inventory/form", view);
        assertTrue(model.containsAttribute("form"), "The template needs a form object to bind to");
        assertEquals("/inventory", model.getAttribute("action"), "The form should post to the collection");
    }

    @Test void createsTheItemInTheOrganisationOfTheLoggedInUser()
    {
        // Arrange
        InventoryItemForm form = validForm();
        BindingResult result = new BeanPropertyBindingResult(form, "form");

        // Act
        String view = controller.create(form, result, staffPrincipal(), new ExtendedModelMap());

        // Assert
        assertEquals("redirect:/inventory?created", view);
        verify(service).create(organisationId, form.toDetails());
    }

    @Test void returnsTheFormWithoutSavingWhenValidationFails()
    {
        // Arrange
        InventoryItemForm form = validForm();
        BindingResult result = new BeanPropertyBindingResult(form, "form");
        result.rejectValue("name", "blank", "Name is required");
        Model model = new ExtendedModelMap();

        // Act
        String view = controller.create(form, result, staffPrincipal(), model);

        // Assert
        assertEquals("inventory/form", view, "The form should be shown again with its errors");
        assertEquals("/inventory", model.getAttribute("action"), "The form must still post to the right place");
        verify(service, never()).create(any(UUID.class), any(InventoryItemDetails.class));
    }

    @Test void showEditFormFillsTheFormFromTheItemOfTheUsersOrganisation()
    {
        // Arrange
        UUID itemId = UUID.randomUUID();
        InventoryItem item = new InventoryItem(new Organisation("Clinic A"), "Patient contacts",
                "Booking appointments", "Practice software", true);
        when(service.get(organisationId, itemId)).thenReturn(item);
        Model model = new ExtendedModelMap();

        // Act
        String view = controller.showEditForm(itemId, staffPrincipal(), model);

        // Assert
        assertEquals("inventory/form", view);
        InventoryItemForm form = (InventoryItemForm) model.getAttribute("form");
        assertEquals("Patient contacts", form.getName());
        assertEquals("/inventory/" + itemId, model.getAttribute("action"));
    }
}