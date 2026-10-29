package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RegistrationControllerTest
{
    private final RegistrationService service = mock(RegistrationService.class);
    private final RegistrationController controller = new RegistrationController(service);

    private RegistrationForm validForm()
    {
        RegistrationForm form = new RegistrationForm();
        form.setOrganisationName("Test Clinic");
        form.setEmail("owner@example.com");
        form.setPassword("a-long-enough-password");
        return form;
    }

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

    @Test void redirectsToLoginAfterSuccessfulRegistration()
    {
        // Arrange
        RegistrationForm form = validForm();
        BindingResult result = new BeanPropertyBindingResult(form, "form");

        // Act
        String view = controller.register(form, result);

        // Assert
        assertEquals("redirect:/login?registered", view, "A successful registration should lead to the login page");
        verify(service).register(any(RegistrationRequest.class));
    }

    @Test void returnsFormWithoutCallingServiceWhenValidationFails()
    {
        // Arrange
        RegistrationForm form = validForm();
        BindingResult result = new BeanPropertyBindingResult(form, "form");
        result.rejectValue("email", "invalid", "Invalid email");

        // Act
        String view = controller.register(form, result);

        // Assert
        assertEquals("register", view, "The form should be shown again with its errors");
        verify(service, never()).register(any(RegistrationRequest.class));
    }
}