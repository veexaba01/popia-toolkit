package com.veex.popia_toolkit.inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID>
{
    List<InventoryItem> findAllByOrganisationIdOrderByNameAsc(UUID organisationId);

    Optional<InventoryItem> findByIdAndOrganisationId(UUID id, UUID organisationId);
}