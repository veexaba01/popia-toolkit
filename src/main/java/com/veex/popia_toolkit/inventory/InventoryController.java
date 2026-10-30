package com.veex.popia_toolkit.inventory;

import com.veex.popia_toolkit.auth.AppUserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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

    @GetMapping("/inventory/new")
    public String showNewForm(Model model)
    {
        model.addAttribute("form", new InventoryItemForm());
        prepareForm(model, "Add an inventory item", "/inventory");
        return "inventory/form";
    }

    @PostMapping("/inventory")
    public String create(@Valid @ModelAttribute("form") InventoryItemForm form,
                         BindingResult result,
                         @AuthenticationPrincipal AppUserPrincipal principal,
                         Model model)
    {
        if (result.hasErrors())
        {
            prepareForm(model, "Add an inventory item", "/inventory");
            return "inventory/form";
        }

        inventoryService.create(principal.getOrganisationId(), form.toDetails());

        return "redirect:/inventory?created";
    }

    @GetMapping("/inventory/{id}/edit")
    public String showEditForm(@PathVariable UUID id,
                               @AuthenticationPrincipal AppUserPrincipal principal,
                               Model model)
    {
        InventoryItem item = inventoryService.get(principal.getOrganisationId(), id);

        model.addAttribute("form", InventoryItemForm.from(item));
        prepareForm(model, "Edit inventory item", "/inventory/" + id);
        return "inventory/form";
    }

    private void prepareForm(Model model, String heading, String action)
    {
        model.addAttribute("heading", heading);
        model.addAttribute("action", action);
    }
}