package com.ecommerce.inventory.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkuDto {
    private Long id;
    private String skuCode;
    private String name;
    private String attributes;
    private BigDecimal price;
    private Integer quantity;
    private Long productId;
    private String productName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

