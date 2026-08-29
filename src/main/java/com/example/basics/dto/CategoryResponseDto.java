package com.example.basics.dto;

public class CategoryResponseDto {

    private Long id;
    private String name;
    private String description;
    private int itemCount;

    public CategoryResponseDto() {
    }

    public CategoryResponseDto(Long id, String name, String description, int itemCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.itemCount = itemCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
