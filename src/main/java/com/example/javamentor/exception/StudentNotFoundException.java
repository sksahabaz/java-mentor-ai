package com.example.javamentor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a student id does not exist.
 * @ResponseStatus makes Spring answer 404 Not Found instead of 500.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class StudentNotFoundException extends RuntimeException {

	public StudentNotFoundException(Long id) {
		super("Student not found with id " + id);
	}
}
