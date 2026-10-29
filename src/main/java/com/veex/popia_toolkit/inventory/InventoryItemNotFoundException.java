package com.veex.popia_toolkit.inventory;

public class InventoryItemNotFoundException extends RuntimeException
{
    public InventoryItemNotFoundException()
    {
        super("Inventory item not found");
    }
}