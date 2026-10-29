package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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
}