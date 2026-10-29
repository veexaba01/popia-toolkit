package com.veex.popia_toolkit.auth;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RegistrationFormTest
{
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private RegistrationForm validForm()
    {
        RegistrationForm form = new RegistrationForm();
        form.setOrganisationName("Test Clinic");
        form.setEmail("owner@example.com");
        form.setPassword("a-long-enough-password");
        return form;
    }

    @Test void rejectsPasswordShorterThanTwelveCharacters()
    {
        // Arrange
        RegistrationForm form = validForm();
        form.setPassword("short");

        // Act and Assert
        assertFalse(validator.validate(form).isEmpty(), "A short password should fail validation");
    }

    @Test void toStringDoesNotRevealPassword()
    {
        // Arrange
        RegistrationForm form = validForm();

        // Act
        String text = form.toString();

        // Assert
        assertFalse(text.contains("a-long-enough-password"), "The password must never appear in the text form");
        assertEquals("RegistrationForm[organisationName=Test Clinic, email=owner@example.com, password=***]", text);
    }
}