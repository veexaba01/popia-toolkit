package com.veex.popia_toolkit.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InventoryItemNotFoundException extends RuntimeException
{
    public InventoryItemNotFoundException()
    {
        super("Inventory item not found");
    }
}