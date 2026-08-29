package com.example.basics.service;

import com.example.basics.dto.CategoryRequestDto;
import com.example.basics.dto.CategoryResponseDto;
import com.example.basics.exception.ResourceNotFoundException;
import com.example.basics.model.Category;
import com.example.basics.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category with name '" + dto.getName() + "' already exists.");
        }
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return mapToResponseDto(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return mapToResponseDto(category);
    }

    private CategoryResponseDto mapToResponseDto(Category category) {
        int itemCount = category.getItems() != null ? category.getItems().size() : 0;
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                itemCount
        );
    }
}
