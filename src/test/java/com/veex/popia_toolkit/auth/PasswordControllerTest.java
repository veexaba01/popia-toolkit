package com.veex.popia_toolkit.auth;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.UUID;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PasswordControllerTest
{
    private final PasswordChangeService service = mock(PasswordChangeService.class);
    private final PasswordController controller = new PasswordController(service);
    private final UUID userId = UUID.randomUUID();

    private AppUserPrincipal principal()
    {
        return new AppUserPrincipal(userId, UUID.randomUUID(), "staff@example.com", "hash", Role.STAFF, true);
    }

    private ChangePasswordForm validForm()
    {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("old-password-1234");
        form.setNewPassword("new-password-5678");
        form.setConfirmPassword("new-password-5678");
        return form;
    }

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

    @Test void changesThePasswordOfTheLoggedInUser()
    {
        // Arrange
        ChangePasswordForm form = validForm();
        BindingResult result = new BeanPropertyBindingResult(form, "form");

        // Act
        String view = controller.change(form, result, principal());

        // Assert
        assertEquals("redirect:/account/password?changed", view);
        verify(service).changePassword(userId, "old-password-1234", "new-password-5678");
    }

    @Test void returnsTheFormWithoutChangingAnythingWhenValidationFails()
    {
        // Arrange
        ChangePasswordForm form = validForm();
        BindingResult result = new BeanPropertyBindingResult(form, "form");
        result.rejectValue("newPassword", "size", "Too short");

        // Act
        String view = controller.change(form, result, principal());

        // Assert
        assertEquals("account/password", view);
        verify(service, never()).changePassword(any(UUID.class), anyString(), anyString());
    }

    @Test void showsAnErrorOnTheCurrentPasswordWhenItIsWrong()
    {
        // Arrange
        ChangePasswordForm form = validForm();
        BindingResult result = new BeanPropertyBindingResult(form, "form");
        doThrow(new InvalidCurrentPasswordException()).when(service)
                .changePassword(any(UUID.class), anyString(), anyString());

        // Act
        String view = controller.change(form, result, principal());

        // Assert
        assertEquals("account/password", view);
        assertTrue(result.hasFieldErrors("currentPassword"), "The error belongs on the current password field");
    }
}