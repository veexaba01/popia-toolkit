package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegistrationControllerTest
{
    private final RegistrationController controller = new RegistrationController();

    @Test void showFormAddsEmptyFormAndReturnsRegisterView()
    {
        // Arrange
        Model model = new ExtendedModelMap();

        // Act
        String view = controller.showForm(model);

        // Assert
        assertEquals("register", view, "The registration template should be shown");
        assertTrue(model.containsAttribute("form"), "The template needs a form object to bind to");
    }
}