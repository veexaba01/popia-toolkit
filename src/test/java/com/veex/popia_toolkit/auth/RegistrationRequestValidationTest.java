package com.veex.popia_toolkit.auth;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegistrationRequestValidationTest
{
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static final String VALID_PASSWORD = "a-long-enough-password";

    @Test void acceptsValidRequest()
    {
        RegistrationRequest request = new RegistrationRequest("Test Clinic", "owner@example.com", VALID_PASSWORD);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test void rejectsBlankOrganisationName()
    {
        RegistrationRequest request = new RegistrationRequest("  ", "owner@example.com", VALID_PASSWORD);

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test void rejectsInvalidEmail()
    {
        RegistrationRequest request = new RegistrationRequest("Test Clinic", "not-an-email", VALID_PASSWORD);

        assertFalse(validator.validate(request).isEmpty());
    }
}