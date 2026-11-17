package com.example.javamentor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.javamentor.entity.Student;
import com.example.javamentor.service.StudentService;

/**
 * REST API for students:
 *   GET    /api/students        -> list all
 *   GET    /api/students/{id}   -> one student
 *   POST   /api/students        -> create   (201 Created)
 *   PUT    /api/students/{id}   -> update
 *   DELETE /api/students/{id}   -> delete   (204 No Content)
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

	private final StudentService studentService;

	public StudentController(StudentService studentService) {
		this.studentService = studentService;
	}

	@GetMapping
	public List<Student> getAll() {
		return studentService.getAll();
	}

	@GetMapping("/{id}")
	public Student getById(@PathVariable Long id) {
		return studentService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Student create(@RequestBody Student student) {
		return studentService.create(student);
	}

	@PutMapping("/{id}")
	public Student update(@PathVariable Long id, @RequestBody Student student) {
		return studentService.update(id, student);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		studentService.delete(id);
	}
}
