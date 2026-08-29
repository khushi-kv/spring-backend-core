package com.example.basics.controller;

import com.example.basics.dto.ItemRequestDto;
import com.example.basics.dto.ItemResponseDto;
import com.example.basics.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/items")
@Tag(name = "Item Controller", description = "REST APIs for Inventory Item CRUD with Pagination & Sorting")
public class ItemRestController {

    private final ItemService itemService;

    @Autowired
    public ItemRestController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @Operation(summary = "Create Inventory Item", description = "Creates a new inventory item linked to a category.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Item created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error / Duplicate SKU"),
        @ApiResponse(responseCode = "404", description = "Category ID not found")
    })
    public ResponseEntity<ItemResponseDto> createItem(@Valid @RequestBody ItemRequestDto dto) {
        ItemResponseDto createdItem = itemService.createItem(dto);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get All Items (Paginated & Sorted)", description = "Retrieves items with pagination and sorting options.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved page of items")
    public ResponseEntity<Page<ItemResponseDto>> getAllItems(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort property", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(itemService.getAllItems(pageable));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get Items by Category (Paginated)", description = "Retrieves paginated items for a specific category.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved page of items")
    public ResponseEntity<Page<ItemResponseDto>> getItemsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(itemService.getItemsByCategoryId(categoryId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Item by ID", description = "Fetches details of an item by database ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item found"),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<ItemResponseDto> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Inventory Item", description = "Updates an existing item's name, price, quantity, or category.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item updated successfully"),
        @ApiResponse(responseCode = "404", description = "Item or Category not found")
    })
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long id, @Valid @RequestBody ItemRequestDto dto) {
        ItemResponseDto updatedItem = itemService.updateItem(id, dto);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Item", description = "Deletes an inventory item by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Item deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
