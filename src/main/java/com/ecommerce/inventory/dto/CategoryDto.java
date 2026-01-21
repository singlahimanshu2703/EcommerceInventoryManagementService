package com.ecommerce.inventory.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private Integer productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

