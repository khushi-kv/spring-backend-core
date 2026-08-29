package com.example.basics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.basics.dto.ItemRequestDto;
import com.example.basics.dto.ItemResponseDto;
import com.example.basics.exception.ResourceNotFoundException;
import com.example.basics.model.Category;
import com.example.basics.model.Item;
import com.example.basics.repository.CategoryRepository;
import com.example.basics.repository.ItemRepository;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ItemResponseDto createItem(ItemRequestDto dto) {
        if (itemRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("Item with SKU '" + dto.getSku() + "' already exists.");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));

        Item item = new Item();
        item.setName(dto.getName());
        item.setSku(dto.getSku());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        item.setCategory(category);

        Item savedItem = itemRepository.save(item);
        return mapToResponseDto(savedItem);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponseDto> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponseDto> getItemsByCategoryId(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with ID: " + categoryId);
        }
        return itemRepository.findByCategoryId(categoryId, pageable)
                .map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public ItemResponseDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));
        return mapToResponseDto(item);
    }

    @Transactional
    public ItemResponseDto updateItem(Long id, ItemRequestDto dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));
        item.setName(dto.getName());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        item.setCategory(category);
        Item updatedItem = itemRepository.save(item);
        return mapToResponseDto(updatedItem);
    }

    @Transactional
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with ID: " + id);
        }
        itemRepository.deleteById(id);
    }

    private ItemResponseDto mapToResponseDto(Item item) {
        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getSku(),
                item.getPrice(),
                item.getQuantity(),
                item.getCategory().getId(),
                item.getCategory().getName()
        );
    }
}
