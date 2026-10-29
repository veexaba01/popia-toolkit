package com.veex.popia_toolkit.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InventoryItemForm
{
    @NotBlank @Size(max = 200)
    private String name;

    @NotBlank @Size(max = 500)
    private String purpose;

    @NotBlank @Size(max = 200)
    private String storageLocation;

    private boolean specialInformation;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public boolean isSpecialInformation() { return specialInformation; }
    public void setSpecialInformation(boolean specialInformation) { this.specialInformation = specialInformation; }

    public InventoryItemDetails toDetails()
    {
        return new InventoryItemDetails(name.trim(), purpose.trim(), storageLocation.trim(), specialInformation);
    }
}