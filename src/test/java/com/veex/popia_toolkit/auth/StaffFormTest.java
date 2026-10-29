package com.veex.popia_toolkit.auth;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StaffFormTest
{
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private StaffForm validForm()
    {
        StaffForm form = new StaffForm();
        form.setEmail("staff@example.com");
        form.setPassword("a-long-enough-password");
        return form;
    }

    @Test void acceptsValidInput()
    {
        // Act and Assert
        assertTrue(validator.validate(validForm()).isEmpty(), "A valid form should pass");
    }

    @Test void rejectsInvalidEmailAndShortPassword()
    {
        // Arrange
        StaffForm form = new StaffForm();
        form.setEmail("not-an-email");
        form.setPassword("short");

        // Act and Assert
        assertEquals(2, validator.validate(form).size(), "Both fields should be reported");
    }

    @Test void toStringDoesNotRevealPassword()
    {
        // Act
        String text = validForm().toString();

        // Assert
        assertFalse(text.contains("a-long-enough-password"), "The password must never appear in the text form");
        assertEquals("StaffForm[email=staff@example.com, password=***]", text);
    }
}