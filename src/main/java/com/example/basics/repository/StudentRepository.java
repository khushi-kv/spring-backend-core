package com.example.basics.repository;

import com.example.basics.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * By extending JpaRepository<Student, Long>, Spring Data JPA automatically gives us:
 * - save(student) -> INSERT / UPDATE
 * - findAll() -> SELECT * FROM students
 * - findById(id) -> SELECT * FROM students WHERE id = ?
 * - deleteById(id) -> DELETE FROM students WHERE id = ?
 * No manual implementation or SQL code required!
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
}

