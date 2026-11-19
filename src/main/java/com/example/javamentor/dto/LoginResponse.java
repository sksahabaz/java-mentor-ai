package com.example.javamentor.dto;

public record LoginResponse(String accessToken,

		String tokenType, long expiresIn, String refreshToken) {
}
