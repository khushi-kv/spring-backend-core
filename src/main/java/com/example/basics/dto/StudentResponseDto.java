package com.example.basics.dto;

/**
 * Data Transfer Object (DTO) for outgoing API responses.
 * Hides internal entity details from API consumers.
 */
public class StudentResponseDto {

    private Long id;
    private String name;
    private String course;

    public StudentResponseDto() {
    }

    public StudentResponseDto(Long id, String name, String course) {
        this.id = id;
        this.name = name;
        this.course = course;
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

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
