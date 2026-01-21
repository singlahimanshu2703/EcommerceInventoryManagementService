package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSkuRequest {

    @Size(min = 3, max = 50, message = "SKU code must be between 3 and 50 characters")
    private String skuCode;

    @Size(min = 2, max = 200, message = "SKU name must be between 2 and 200 characters")
    private String name;

    @Size(max = 500, message = "Attributes cannot exceed 500 characters")
    private String attributes;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}

