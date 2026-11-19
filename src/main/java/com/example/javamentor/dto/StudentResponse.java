package com.example.javamentor.dto;

import com.example.javamentor.entity.Student;

public record StudentResponse(

		Long id,
		String name,
		String email,
		String phone,
		String course,
		String level
		) {
	public static StudentResponse from(Student s) {
		return new StudentResponse(
			s.getId(),
			s.getName(),
			s.getEmail(),
			s.getPhone(),
			s.getCourse(),
			s.getLevel()
				);
	}
}
