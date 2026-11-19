package com.example.javamentor.dto;

import java.time.Instant;
import java.util.Map;

public record ApiError(
		Instant timestamp,
		int status,
		String error,
		String message,
		Map<String, String> errors) {
	public static ApiError of(int status, String error, String message) {
		return new ApiError(Instant.now(), status, error, message, Map.of());
	}

	public static ApiError of(int status, String error, String message, Map<String, String> errors) {
		return new ApiError(Instant.now(), status, error, message, errors);
	}
}
