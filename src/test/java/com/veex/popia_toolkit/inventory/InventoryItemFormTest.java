package com.veex.popia_toolkit.inventory;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryItemFormTest
{
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private InventoryItemForm validForm()
    {
        InventoryItemForm form = new InventoryItemForm();
        form.setName("Patient contacts");
        form.setPurpose("Booking appointments");
        form.setStorageLocation("Practice software");
        form.setSpecialInformation(false);
        return form;
    }

    @Test void acceptsValidInput()
    {
        // Act and Assert
        assertTrue(validator.validate(validForm()).isEmpty(), "A valid form should pass");
    }

    @Test void rejectsABlankName()
    {
        // Arrange
        InventoryItemForm form = validForm();
        form.setName("   ");

        // Act and Assert
        assertFalse(validator.validate(form).isEmpty(), "A name made of spaces should fail");
    }

    @Test void rejectsAPurposeLongerThanFiveHundredCharacters()
    {
        // Arrange
        InventoryItemForm form = validForm();
        form.setPurpose("a".repeat(501));

        // Act and Assert
        assertFalse(validator.validate(form).isEmpty(), "The purpose must fit the database column");
    }

    @Test void convertsToDetailsWithTrimmedValues()
    {
        // Arrange
        InventoryItemForm form = validForm();
        form.setName("  Patient contacts  ");
        form.setSpecialInformation(true);

        // Act
        InventoryItemDetails details = form.toDetails();

        // Assert
        assertEquals(new InventoryItemDetails("Patient contacts", "Booking appointments", "Practice software", true),
                details, "Surrounding spaces should be removed");
    }
}