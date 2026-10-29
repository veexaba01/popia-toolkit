package com.veex.popia_toolkit.inventory;

import com.veex.popia_toolkit.organisation.Organisation;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
public class InventoryItem
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String purpose;

    @Column(name = "storage_location", nullable = false, length = 200)
    private String storageLocation;

    @Column(name = "special_information", nullable = false)
    private boolean specialInformation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected InventoryItem() {}

    public InventoryItem(Organisation organisation, String name, String purpose,
                         String storageLocation, boolean specialInformation)
    {
        this.organisation = organisation;
        this.name = name;
        this.purpose = purpose;
        this.storageLocation = storageLocation;
        this.specialInformation = specialInformation;
    }

    public UUID getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public String getName() { return name; }
    public String getPurpose() { return purpose; }
    public String getStorageLocation() { return storageLocation; }
    public boolean isSpecialInformation() { return specialInformation; }
    public Instant getCreatedAt() { return createdAt; }
}