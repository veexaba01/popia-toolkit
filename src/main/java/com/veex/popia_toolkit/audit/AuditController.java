package com.veex.popia_toolkit.audit;

import com.veex.popia_toolkit.auth.AppUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditController
{
    private final AuditService auditService;

    public AuditController(AuditService auditService)
    {
        this.auditService = auditService;
    }

    @GetMapping("/admin/audit")
    public String show(@AuthenticationPrincipal AppUserPrincipal principal, Model model)
    {
        model.addAttribute("events", auditService.recent(principal.getOrganisationId()));
        return "admin/audit";
    }
}