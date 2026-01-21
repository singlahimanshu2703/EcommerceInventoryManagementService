package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.exception.GlobalExceptionHandler;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        productDto = ProductDto.builder()
                .id(1L)
                .name("iPhone 15")
                .description("Latest iPhone model")
                .basePrice(new BigDecimal("999.99"))
                .brand("Apple")
                .categoryId(1L)
                .categoryName("Electronics")
                .skuCount(0)
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return paginated products")
        void shouldReturnPaginatedProducts() throws Exception {
            PagedResponse<ProductDto> pagedResponse = PagedResponse.<ProductDto>builder()
                    .content(List.of(productDto))
                    .page(0)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(productService.getAllProducts(any(), any(), anyInt(), anyInt())).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/products")
                            .param("page", "0")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].name").value("iPhone 15"));
        }

        @Test
        @DisplayName("Should filter products by name")
        void shouldFilterProductsByName() throws Exception {
            PagedResponse<ProductDto> pagedResponse = PagedResponse.<ProductDto>builder()
                    .content(List.of(productDto))
                    .page(0)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(productService.getAllProducts(eq("iPhone"), any(), anyInt(), anyInt())).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/products")
                            .param("name", "iPhone"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].name").value("iPhone 15"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when found")
        void shouldReturnProductWhenFound() throws Exception {
            when(productService.getProductById(1L)).thenReturn(productDto);

            mockMvc.perform(get("/api/v1/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("iPhone 15"));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            when(productService.getProductById(999L))
                    .thenThrow(new ResourceNotFoundException("Product", "id", 999L));

            mockMvc.perform(get("/api/v1/products/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/products")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() throws Exception {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("iPhone 15")
                    .description("Latest iPhone model")
                    .basePrice(new BigDecimal("999.99"))
                    .brand("Apple")
                    .categoryId(1L)
                    .build();

            when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(productDto);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("iPhone 15"));
        }

        @Test
        @DisplayName("Should return 400 for invalid request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("")
                    .build();

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/{id}")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() throws Exception {
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .basePrice(new BigDecimal("1099.99"))
                    .build();

            when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class))).thenReturn(productDto);

            mockMvc.perform(put("/api/v1/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() throws Exception {
            doNothing().when(productService).deleteProduct(1L);

            mockMvc.perform(delete("/api/v1/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}

