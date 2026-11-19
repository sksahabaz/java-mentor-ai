package com.example.javamentor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.javamentor.entity.QuizAttempt;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
	List<QuizAttempt> findByStudentIdOrderByAttemptedAtDesc(Long studentId);

	void deleteByStudentId(Long studentId);
}
