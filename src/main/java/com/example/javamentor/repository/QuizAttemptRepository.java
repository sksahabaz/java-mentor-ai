package com.example.javamentor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.javamentor.entity.QuizAttempt;

/**
 * Spring Data builds the SQL from the method names:
 *   findByStudentIdOrderByAttemptedAtDesc -> WHERE student_id = ? ORDER BY attempted_at DESC
 *   deleteByStudentId                     -> DELETE ... WHERE student_id = ?
 */
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

	List<QuizAttempt> findByStudentIdOrderByAttemptedAtDesc(Long studentId);

	void deleteByStudentId(Long studentId);
}
