package com.veex.popia_toolkit.auth;

import com.veex.popia_toolkit.audit.AuditAction;
import com.veex.popia_toolkit.audit.AuditService;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminStaffController
{
    private final StaffService staffService;
    private final AuditService auditService;

    public AdminStaffController(StaffService staffService, AuditService auditService)
    {
        this.staffService = staffService;
        this.auditService = auditService;
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
        if (result.hasErrors())
        {
            return "admin/staff";
        }

        try
        {
            staffService.createStaff(principal.getOrganisationId(), form.getEmail(), form.getPassword());
        }
        catch (EmailAlreadyRegisteredException e)
        {
            result.reject("staff.failed", "Staff member could not be added with these details.");
            return "admin/staff";
        }

        auditService.record(principal.getOrganisationId(), principal.getUsername(), AuditAction.STAFF_CREATED,
                "Staff: " + form.getEmail().trim().toLowerCase(Locale.ROOT));

        return "redirect:/admin/staff?created";
    }
}