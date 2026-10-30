package com.veex.popia_toolkit.auth;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChangePasswordFormTest
{
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private ChangePasswordForm validForm()
    {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("old-password-1234");
        form.setNewPassword("new-password-5678");
        form.setConfirmPassword("new-password-5678");
        return form;
    }

    @Test void acceptsValidInput()
    {
        assertTrue(validator.validate(validForm()).isEmpty(), "A valid form should pass");
    }

    @Test void rejectsANewPasswordShorterThanTwelveCharacters()
    {
        // Arrange
        ChangePasswordForm form = validForm();
        form.setNewPassword("short");

        // Act and Assert
        assertFalse(validator.validate(form).isEmpty(), "A short new password should fail");
    }

    @Test void toStringDoesNotRevealAnyPassword()
    {
        // Act
        String text = validForm().toString();

        // Assert
        assertFalse(text.contains("old-password-1234"), "The current password must never be printed");
        assertFalse(text.contains("new-password-5678"), "The new password must never be printed");
        assertEquals("ChangePasswordForm[currentPassword=***, newPassword=***, confirmPassword=***]", text);
    }
}