package com.ecommerce.inventory.mapper;

import com.ecommerce.inventory.dto.CreateSkuRequest;
import com.ecommerce.inventory.dto.SkuDto;
import com.ecommerce.inventory.entity.Sku;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SkuMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    SkuDto toDto(Sku sku);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Sku toEntity(CreateSkuRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(@MappingTarget Sku sku, com.ecommerce.inventory.dto.UpdateSkuRequest request);
}

