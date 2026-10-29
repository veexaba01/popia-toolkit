package com.veex.popia_toolkit.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminStaffController
{
    private final StaffService staffService;

    public AdminStaffController(StaffService staffService)
    {
        this.staffService = staffService;
    }

    @GetMapping("/admin/staff")
    public String showForm(Model model)
    {
        model.addAttribute("form", new StaffForm());
        return "admin/staff";
    }

    @PostMapping("/admin/staff")
    public String create(@Valid @ModelAttribute("form") StaffForm form,
                         BindingResult result,
                         @AuthenticationPrincipal AppUserPrincipal principal)
    {
        staffService.createStaff(principal.getOrganisationId(), form.getEmail(), form.getPassword());

        return "redirect:/admin/staff?created";
    }
}