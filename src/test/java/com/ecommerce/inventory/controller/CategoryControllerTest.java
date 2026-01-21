package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.exception.DuplicateResourceException;
import com.ecommerce.inventory.exception.GlobalExceptionHandler;
import com.ecommerce.inventory.exception.InvalidOperationException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.service.CategoryService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .productCount(0)
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/categories")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should return all categories")
        void shouldReturnAllCategories() throws Exception {
            when(categoryService.getAllCategories()).thenReturn(List.of(categoryDto));

            mockMvc.perform(get("/api/v1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].name").value("Electronics"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/{id}")
    class GetCategoryByIdTests {

        @Test
        @DisplayName("Should return category when found")
        void shouldReturnCategoryWhenFound() throws Exception {
            when(categoryService.getCategoryById(1L)).thenReturn(categoryDto);

            mockMvc.perform(get("/api/v1/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Electronics"));
        }

        @Test
        @DisplayName("Should return 404 when category not found")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            when(categoryService.getCategoryById(999L))
                    .thenThrow(new ResourceNotFoundException("Category", "id", 999L));

            mockMvc.perform(get("/api/v1/categories/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/categories")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategorySuccessfully() throws Exception {
            CreateCategoryRequest request = CreateCategoryRequest.builder()
                    .name("Electronics")
                    .description("Electronic devices")
                    .build();

            when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(categoryDto);

            mockMvc.perform(post("/api/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Electronics"));
        }

        @Test
        @DisplayName("Should return 400 for invalid request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            CreateCategoryRequest request = CreateCategoryRequest.builder()
                    .name("")
                    .build();

            mockMvc.perform(post("/api/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 for duplicate category")
        void shouldReturn409ForDuplicateCategory() throws Exception {
            CreateCategoryRequest request = CreateCategoryRequest.builder()
                    .name("Electronics")
                    .description("Electronic devices")
                    .build();

            when(categoryService.createCategory(any(CreateCategoryRequest.class)))
                    .thenThrow(new DuplicateResourceException("Category", "name", "Electronics"));

            mockMvc.perform(post("/api/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/categories/{id}")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() throws Exception {
            UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                    .name("Updated Electronics")
                    .build();

            when(categoryService.updateCategory(eq(1L), any(UpdateCategoryRequest.class))).thenReturn(categoryDto);

            mockMvc.perform(put("/api/v1/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/categories/{id}")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category successfully")
        void shouldDeleteCategorySuccessfully() throws Exception {
            doNothing().when(categoryService).deleteCategory(1L);

            mockMvc.perform(delete("/api/v1/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 400 when category has products")
        void shouldReturn400WhenCategoryHasProducts() throws Exception {
            doThrow(new InvalidOperationException("Cannot delete category with products"))
                    .when(categoryService).deleteCategory(1L);

            mockMvc.perform(delete("/api/v1/categories/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}

