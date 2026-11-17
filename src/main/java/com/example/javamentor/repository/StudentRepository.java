package com.example.javamentor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.javamentor.entity.Student;

/**
 * Spring Data JPA writes the implementation for us:
 * findAll, findById, save, deleteById, existsById ... are all ready to use.
 * JpaRepository<Student, Long> = entity type, type of its @Id.
 */
public interface StudentRepository extends JpaRepository<Student, Long> {
}
