package com.veex.popia_toolkit.inventory;

import com.veex.popia_toolkit.organisation.Organisation;
import com.veex.popia_toolkit.organisation.OrganisationRepository;
import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService
{
    private final InventoryItemRepository items;
    private final OrganisationRepository organisations;

    public InventoryService(InventoryItemRepository items, OrganisationRepository organisations)
    {
        this.items = items;
        this.organisations = organisations;
    }

    @Transactional
    public InventoryItem create(UUID organisationId, InventoryItemDetails details)
    {
        Organisation organisation = organisations.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        return items.save(new InventoryItem(organisation, details.name(), details.purpose(),
                details.storageLocation(), details.specialInformation()));
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> list(UUID organisationId)
    {
        return items.findAllByOrganisationIdOrderByNameAsc(organisationId);
    }

    @Transactional(readOnly = true)
    public InventoryItem get(UUID organisationId, UUID itemId)
    {
        return items.findByIdAndOrganisationId(itemId, organisationId)
                .orElseThrow(InventoryItemNotFoundException::new);
    }

    @Transactional
    public InventoryItem update(UUID organisationId, UUID itemId, InventoryItemDetails details)
    {
        InventoryItem item = get(organisationId, itemId);
        item.update(details.name(), details.purpose(), details.storageLocation(), details.specialInformation());

        return items.save(item);
    }
}