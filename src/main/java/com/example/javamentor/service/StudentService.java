package com.example.javamentor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.javamentor.entity.Student;
import com.example.javamentor.exception.StudentNotFoundException;
import com.example.javamentor.repository.QuizAttemptRepository;
import com.example.javamentor.repository.StudentRepository;

/**
 * Business logic for students. Controller -> Service -> Repository -> MySQL.
 */
@Service
public class StudentService {

	private final StudentRepository studentRepository;
	private final QuizAttemptRepository quizAttemptRepository;

	public StudentService(StudentRepository studentRepository, QuizAttemptRepository quizAttemptRepository) {
		this.studentRepository = studentRepository;
		this.quizAttemptRepository = quizAttemptRepository;
	}

	public List<Student> getAll() {
		return studentRepository.findAll();
	}

	public Student getById(Long id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> new StudentNotFoundException(id));
	}

	public Student create(Student student) {
		student.setId(null); // always INSERT a new row, never overwrite an existing one
		return studentRepository.save(student);
	}

	public Student update(Long id, Student updated) {
		Student existing = getById(id); // 404 if it doesn't exist
		existing.setName(updated.getName());
		existing.setEmail(updated.getEmail());
		existing.setPhone(updated.getPhone());
		existing.setCourse(updated.getCourse());
		existing.setLevel(updated.getLevel());
		return studentRepository.save(existing); // id is set, so save() = UPDATE
	}

	// @Transactional: both deletes succeed together, or neither happens
	@Transactional
	public void delete(Long id) {
		if (!studentRepository.existsById(id)) {
			throw new StudentNotFoundException(id);
		}
		quizAttemptRepository.deleteByStudentId(id); // child rows first (foreign key)
		studentRepository.deleteById(id);
	}
}
