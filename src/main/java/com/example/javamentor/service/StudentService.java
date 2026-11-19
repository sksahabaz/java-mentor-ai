package com.example.javamentor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.javamentor.dto.UpdateProfileRequest;
import com.example.javamentor.entity.Student;
import com.example.javamentor.exception.StudentNotFoundException;
import com.example.javamentor.repository.QuizAttemptRepository;
import com.example.javamentor.repository.RefreshTokenRepository;
import com.example.javamentor.repository.StudentRepository;

@Service
public class StudentService {
	private final StudentRepository studentRepository;
	private final QuizAttemptRepository quizAttemptRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	public StudentService(StudentRepository studentRepository, QuizAttemptRepository quizAttemptRepository,
			RefreshTokenRepository refreshTokenRepository) {
		this.studentRepository = studentRepository;
		this.quizAttemptRepository = quizAttemptRepository;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	public List<Student> getAll() {
		return studentRepository.findAll();
	}

	public Student getById(Long id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> new StudentNotFoundException(id));
	}

	@Transactional
	public void delete(Long id) {
		if (!studentRepository.existsById(id)) {
			throw new StudentNotFoundException(id);
		}
		quizAttemptRepository.deleteByStudentId(id);
		refreshTokenRepository.deleteByStudentId(id);
		studentRepository.deleteById(id);
	}

	@Transactional
	public Student updateProfile(Long id, UpdateProfileRequest req) {
		Student existing = getById(id);
		existing.setName(req.name());
		existing.setPhone(req.phone());
		existing.setCourse(req.course());
		existing.setLevel(req.level());
		return existing;
	}
}
