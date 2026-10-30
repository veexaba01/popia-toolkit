package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class PasswordControllerTest
{
    private final PasswordChangeService service = mock(PasswordChangeService.class);
    private final PasswordController controller = new PasswordController(service);

    @Test void showFormAddsAnEmptyFormAndReturnsThePasswordView()
    {
        // Arrange
        Model model = new ExtendedModelMap();

        // Act
        String view = controller.showForm(model);

        // Assert
        assertEquals("account/password", view);
        assertTrue(model.containsAttribute("form"), "The template needs a form object to bind to");
    }
}