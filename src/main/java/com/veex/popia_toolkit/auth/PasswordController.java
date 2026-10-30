package com.veex.popia_toolkit.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PasswordController
{
    private final PasswordChangeService passwordChangeService;

    public PasswordController(PasswordChangeService passwordChangeService)
    {
        this.passwordChangeService = passwordChangeService;
    }

    @GetMapping("/account/password")
    public String showForm(Model model)
    {
        model.addAttribute("form", new ChangePasswordForm());
        return "account/password";
    }
}