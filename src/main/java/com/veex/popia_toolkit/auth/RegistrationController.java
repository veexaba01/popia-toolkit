package com.veex.popia_toolkit.auth;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController
{
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService)
    {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String showForm(Model model)
    {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result)
    {
        if (result.hasErrors())
        {
            return "register";
        }
        try
        {
            registrationService.register(
                    new RegistrationRequest(form.getOrganisationName(), form.getEmail(), form.getPassword()));
        }
        catch (EmailAlreadyRegisteredException e)
        {
            result.reject("registration.failed", "Registration could not be completed with these details.");
            return "register";
        }
        return "redirect:/login?registered";
    }
}