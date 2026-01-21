package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.CategoryDto;
import com.ecommerce.inventory.dto.CreateCategoryRequest;
import com.ecommerce.inventory.dto.UpdateCategoryRequest;
import com.ecommerce.inventory.entity.Category;
import com.ecommerce.inventory.entity.Product;
import com.ecommerce.inventory.exception.DuplicateResourceException;
import com.ecommerce.inventory.exception.InvalidOperationException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.mapper.CategoryMapper;
import com.ecommerce.inventory.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryDto categoryDto;
    private CreateCategoryRequest createRequest;
    private UpdateCategoryRequest updateRequest;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .products(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .productCount(0)
                .build();

        createRequest = CreateCategoryRequest.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();

        updateRequest = UpdateCategoryRequest.builder()
                .name("Updated Electronics")
                .description("Updated description")
                .build();
    }

    @Nested
    @DisplayName("Get All Categories Tests")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should return all categories")
        void shouldReturnAllCategories() {
            when(categoryRepository.findAll()).thenReturn(List.of(category));
            when(categoryMapper.toDto(category)).thenReturn(categoryDto);

            List<CategoryDto> result = categoryService.getAllCategories();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Electronics");
            verify(categoryRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no categories exist")
        void shouldReturnEmptyListWhenNoCategoriesExist() {
            when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

            List<CategoryDto> result = categoryService.getAllCategories();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Category By ID Tests")
    class GetCategoryByIdTests {

        @Test
        @DisplayName("Should return category when found")
        void shouldReturnCategoryWhenFound() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryMapper.toDto(category)).thenReturn(categoryDto);

            CategoryDto result = categoryService.getCategoryById(1L);

            assertThat(result.getName()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category not found");
        }
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategorySuccessfully() {
            when(categoryRepository.existsByName(createRequest.getName())).thenReturn(false);
            when(categoryMapper.toEntity(createRequest)).thenReturn(category);
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toDto(category)).thenReturn(categoryDto);

            CategoryDto result = categoryService.createCategory(createRequest);

            assertThat(result.getName()).isEqualTo("Electronics");
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when category name already exists")
        void shouldThrowExceptionWhenCategoryNameExists() {
            when(categoryRepository.existsByName(createRequest.getName())).thenReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory(createRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndIdNot(updateRequest.getName(), 1L)).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryDto);

            CategoryDto result = categoryService.updateCategory(1L, updateRequest);

            assertThat(result).isNotNull();
            verify(categoryMapper).updateEntityFromRequest(any(Category.class), eq(updateRequest));
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when updating to existing name")
        void shouldThrowExceptionWhenUpdatingToExistingName() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndIdNot(updateRequest.getName(), 1L)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.updateCategory(1L, updateRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category successfully when no products")
        void shouldDeleteCategorySuccessfully() {
            category.setProducts(new ArrayList<>());
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            categoryService.deleteCategory(1L);

            verify(categoryRepository).delete(category);
        }

        @Test
        @DisplayName("Should throw exception when category has products")
        void shouldThrowExceptionWhenCategoryHasProducts() {
            Product product = Product.builder().id(1L).name("Test Product").build();
            category.setProducts(List.of(product));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("Cannot delete category");
        }
    }
}

