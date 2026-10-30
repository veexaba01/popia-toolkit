package com.veex.popia_toolkit.auth;

import com.veex.popia_toolkit.audit.AuditAction;
import com.veex.popia_toolkit.audit.AuditService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PasswordController
{
    private final PasswordChangeService passwordChangeService;
    private final AuditService auditService;

    public PasswordController(PasswordChangeService passwordChangeService, AuditService auditService)
    {
        this.passwordChangeService = passwordChangeService;
        this.auditService = auditService;
    }

    @GetMapping("/account/password")
    public String showForm(Model model)
    {
        model.addAttribute("form", new ChangePasswordForm());
        return "account/password";
    }

    @PostMapping("/account/password")
    public String change(@Valid @ModelAttribute("form") ChangePasswordForm form,
                         BindingResult result,
                         @AuthenticationPrincipal AppUserPrincipal principal)
    {
        if (!result.hasErrors())
        {
            if (!form.getNewPassword().equals(form.getConfirmPassword()))
            {
                result.rejectValue("confirmPassword", "mismatch", "The two new passwords do not match.");
            }
            else if (form.getNewPassword().equals(form.getCurrentPassword()))
            {
                result.rejectValue("newPassword", "same", "The new password must be different from the current one.");
            }
        }

        if (result.hasErrors())
        {
            return "account/password";
        }

        try
        {
            passwordChangeService.changePassword(principal.getUserId(), form.getCurrentPassword(),
                    form.getNewPassword());
        }
        catch (InvalidCurrentPasswordException e)
        {
            result.rejectValue("currentPassword", "wrong", "The current password is not correct.");
            return "account/password";
        }

        auditService.record(principal.getOrganisationId(), principal.getUsername(),
                AuditAction.PASSWORD_CHANGED, null);

        return "redirect:/account/password?changed";
    }
}