package com.veex.popia_toolkit.inventory;

import com.veex.popia_toolkit.auth.AppUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryController
{
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService)
    {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory")
    public String list(@AuthenticationPrincipal AppUserPrincipal principal, Model model)
    {
        model.addAttribute("items", inventoryService.list(principal.getOrganisationId()));
        return "inventory/list";
    }
}