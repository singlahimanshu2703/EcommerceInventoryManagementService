package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSkuRequest {

    @NotBlank(message = "SKU code is required")
    @Size(min = 3, max = 50, message = "SKU code must be between 3 and 50 characters")
    private String skuCode;

    @NotBlank(message = "SKU name is required")
    @Size(min = 2, max = 200, message = "SKU name must be between 2 and 200 characters")
    private String name;

    @Size(max = 500, message = "Attributes cannot exceed 500 characters")
    private String attributes;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}

