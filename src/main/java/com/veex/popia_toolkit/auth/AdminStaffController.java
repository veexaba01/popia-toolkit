package com.veex.popia_toolkit.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
}