package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Base price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal basePrice;

    @Size(min = 1, max = 100, message = "Brand must be between 1 and 100 characters")
    private String brand;

    private Long categoryId;
}

