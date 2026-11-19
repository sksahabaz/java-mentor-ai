package com.example.javamentor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
		@NotBlank @Size(max = 100) String name,
		@Size(max = 20) String phone,
		@Size(max = 100) String course,
		@Size(max = 30) String level) {
	public UpdateProfileRequest {
		name = (name == null) ? null : name.trim();
	}
}
