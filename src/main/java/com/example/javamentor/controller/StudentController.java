package com.example.javamentor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.javamentor.dto.StudentResponse;
import com.example.javamentor.dto.UpdateProfileRequest;
import com.example.javamentor.security.CurrentUser;
import com.example.javamentor.service.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
public class StudentController {
	private final StudentService studentService;

	public StudentController(StudentService studentService) {
		this.studentService = studentService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<StudentResponse> getAll() {
		return null;
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public StudentResponse getById(@PathVariable Long id) {
		return null;
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public StudentResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest req) {
		return StudentResponse.from(studentService.updateProfile(id, req));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		studentService.delete(id);
	}

	@GetMapping("/me")
	public StudentResponse me(@AuthenticationPrincipal Jwt jwt) {
		return StudentResponse.from(studentService.getById(CurrentUser.studentId(jwt)));
	}

	@DeleteMapping("/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteMe(@AuthenticationPrincipal Jwt jwt) {
		studentService.delete(CurrentUser.studentId(jwt));
	}

	@PutMapping("/me")
	public StudentResponse updateMe(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateProfileRequest req) {
		return StudentResponse.from(studentService.updateProfile(CurrentUser.studentId(jwt), req));
	}
}
