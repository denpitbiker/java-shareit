package ru.practicum.shareit.item.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static ru.practicum.shareit.TestStubs.INVALID_ITEM_DTO_BLANK_DESCRIPTION;
import static ru.practicum.shareit.TestStubs.INVALID_ITEM_DTO_BLANK_NAME;
import static ru.practicum.shareit.TestStubs.INVALID_ITEM_DTO_NULL_AVAILABLE;
import static ru.practicum.shareit.TestStubs.INVALID_ITEM_DTO_NULL_DESCRIPTION;
import static ru.practicum.shareit.TestStubs.INVALID_ITEM_DTO_NULL_NAME;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_1;

public class ItemDtoTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Valid item dto validation")
    public void validate_validItemDto_noViolations() {
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(VALID_ITEM_DTO_1);
        Assertions.assertTrue(violations.isEmpty(), "Expected valid item dto without violations");
    }

    @Test
    @DisplayName("ItemDto with null name validation")
    public void validate_itemDtoWithNullName_hasViolation() {
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(INVALID_ITEM_DTO_NULL_NAME);
        Assertions.assertFalse(violations.isEmpty(), "Expected violation for null name");
    }

    @Test
    @DisplayName("ItemDto with blank name validation")
    public void validate_itemDtoWithBlankName_hasViolation() {
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(INVALID_ITEM_DTO_BLANK_NAME);
        Assertions.assertFalse(violations.isEmpty(), "Expected violation for blank name");
    }

    @Test
    @DisplayName("ItemDto with null description validation")
    public void validate_itemDtoWithNullDescription_hasViolation() {
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(INVALID_ITEM_DTO_NULL_DESCRIPTION);
        Assertions.assertFalse(violations.isEmpty(), "Expected violation for null description");
    }

    @Test
    @DisplayName("ItemDto with blank description validation")
    public void validate_itemDtoWithBlankDescription_hasViolation() {
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(INVALID_ITEM_DTO_BLANK_DESCRIPTION);
        Assertions.assertFalse(violations.isEmpty(), "Expected violation for blank description");
    }

    @Test
    @DisplayName("ItemDto with null available validation")
    public void validate_itemDtoWithNullAvailable_hasViolation() {
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(INVALID_ITEM_DTO_NULL_AVAILABLE);
        Assertions.assertFalse(violations.isEmpty(), "Expected violation for null available");
    }
}
