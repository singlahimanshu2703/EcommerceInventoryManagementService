package com.ecommerce.inventory.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private Integer skuCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

