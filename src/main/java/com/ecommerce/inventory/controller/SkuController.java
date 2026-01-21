package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.service.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/skus")
@RequiredArgsConstructor
@Tag(name = "SKU", description = "SKU management APIs")
public class SkuController {

    private final SkuService skuService;

    @GetMapping
    @Operation(summary = "Get all SKUs for a product", description = "Retrieves all SKUs belonging to a specific product")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved SKUs"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<List<SkuDto>>> getSkusByProductId(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        List<SkuDto> skus = skuService.getSkusByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(skus));
    }

    @GetMapping("/{skuId}")
    @Operation(summary = "Get SKU by ID", description = "Retrieves a specific SKU by its ID for a product")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved SKU"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or SKU not found")
    })
    public ResponseEntity<ApiResponse<SkuDto>> getSkuById(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        SkuDto sku = skuService.getSkuById(productId, skuId);
        return ResponseEntity.ok(ApiResponse.success(sku));
    }

    @PostMapping
    @Operation(summary = "Create a new SKU", description = "Creates a new SKU for a product")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "SKU created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "SKU with this code already exists")
    })
    public ResponseEntity<ApiResponse<SkuDto>> createSku(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody CreateSkuRequest request) {
        SkuDto createdSku = skuService.createSku(productId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("SKU created successfully", createdSku));
    }

    @PutMapping("/{skuId}")
    @Operation(summary = "Update a SKU", description = "Updates an existing SKU")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SKU updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or SKU not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "SKU with this code already exists")
    })
    public ResponseEntity<ApiResponse<SkuDto>> updateSku(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId,
            @Valid @RequestBody UpdateSkuRequest request) {
        SkuDto updatedSku = skuService.updateSku(productId, skuId, request);
        return ResponseEntity.ok(ApiResponse.success("SKU updated successfully", updatedSku));
    }

    @DeleteMapping("/{skuId}")
    @Operation(summary = "Delete a SKU", description = "Deletes a SKU by its ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SKU deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or SKU not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteSku(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        skuService.deleteSku(productId, skuId);
        return ResponseEntity.ok(ApiResponse.success("SKU deleted successfully", null));
    }
}

