package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.Category;
import com.ecommerce.inventory.exception.DuplicateResourceException;
import com.ecommerce.inventory.exception.InvalidOperationException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.mapper.CategoryMapper;
import com.ecommerce.inventory.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryDto> getAllCategories() {
        log.info("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(Long id) {
        log.info("Fetching category with id: {}", id);
        Category category = findCategoryById(id);
        return categoryMapper.toDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        log.info("Creating new category with name: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully with id: {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Updating category with id: {}", id);

        Category category = findCategoryById(id);

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new DuplicateResourceException("Category", "name", request.getName());
            }
        }

        categoryMapper.updateEntityFromRequest(category, request);
        Category updatedCategory = categoryRepository.save(category);

        log.info("Category updated successfully with id: {}", id);
        return categoryMapper.toDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);

        Category category = findCategoryById(id);

        if (!category.getProducts().isEmpty()) {
            throw new InvalidOperationException(
                    String.format("Cannot delete category '%s' as it has %d associated products",
                            category.getName(), category.getProducts().size()));
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with id: {}", id);
    }

    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}

