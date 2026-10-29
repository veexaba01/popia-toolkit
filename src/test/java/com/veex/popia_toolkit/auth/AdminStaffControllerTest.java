package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import java.util.UUID;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

public class AdminStaffControllerTest
{
    private final StaffService staffService = mock(StaffService.class);
    private final AdminStaffController controller = new AdminStaffController(staffService);

    @Test void showFormAddsEmptyFormAndReturnsStaffView()
    {
        // Arrange
        Model model = new ExtendedModelMap();

        // Act
        String view = controller.showForm(model);

        // Assert
        assertEquals("admin/staff", view, "The staff template should be shown");
        assertTrue(model.containsAttribute("form"), "The template needs a form object to bind to");
    }

        private final UUID organisationId = UUID.randomUUID();

    private AppUserPrincipal adminPrincipal()
    {
        return new AppUserPrincipal(UUID.randomUUID(), organisationId, "owner@example.com", "hash", Role.ADMIN, true);
    }

    private StaffForm validForm()
    {
        StaffForm form = new StaffForm();
        form.setEmail("staff@example.com");
        form.setPassword("a-long-enough-password");
        return form;
    }

    @Test void createsStaffInTheOrganisationOfTheLoggedInAdmin()
    {
        // Arrange
        StaffForm form = validForm();
        BindingResult result = new BeanPropertyBindingResult(form, "form");

        // Act
        String view = controller.create(form, result, adminPrincipal());

        // Assert
        assertEquals("redirect:/admin/staff?created", view, "A successful creation returns to the page with a notice");
        verify(staffService).createStaff(organisationId, "staff@example.com", "a-long-enough-password");
    }

        @Test void returnsFormWithoutCallingServiceWhenValidationFails()
    {
        // Arrange
        StaffForm form = validForm();
        BindingResult result = new BeanPropertyBindingResult(form, "form");
        result.rejectValue("email", "invalid", "Invalid email");

        // Act
        String view = controller.create(form, result, adminPrincipal());

        // Assert
        assertEquals("admin/staff", view, "The form should be shown again with its errors");
        verify(staffService, never()).createStaff(any(UUID.class), anyString(), anyString());
    }

    @Test void showsGenericErrorWhenEmailAlreadyRegistered()
    {
        // Arrange
        StaffForm form = validForm();
        BindingResult result = new BeanPropertyBindingResult(form, "form");
        when(staffService.createStaff(any(UUID.class), anyString(), anyString()))
                .thenThrow(new EmailAlreadyRegisteredException());

        // Act
        String view = controller.create(form, result, adminPrincipal());

        // Assert
        assertEquals("admin/staff", view, "The form should be shown again");
        assertTrue(result.hasGlobalErrors(), "A general error message should be attached to the form");
    }
}