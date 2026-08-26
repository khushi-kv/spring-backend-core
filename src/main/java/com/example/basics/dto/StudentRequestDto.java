package com.example.basics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for incoming Student creation/update requests.
 * Encapsulates validation logic using Bean Validation annotations.
 */
public class StudentRequestDto {

    @NotBlank(message = "Student name cannot be empty")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Course name cannot be empty")
    private String course;

    public StudentRequestDto() {
    }

    public StudentRequestDto(String name, String course) {
        this.name = name;
        this.course = course;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
