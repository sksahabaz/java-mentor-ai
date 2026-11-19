package com.example.javamentor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.javamentor.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
	boolean existsByEmail(String email);
	@EntityGraph(attributePaths = "roles")
	Optional<Student> findByEmail(String email);
}
