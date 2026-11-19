package com.example.javamentor.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidRefreshTokenException extends AuthenticationException {
	private static final long serialVersionUID = 1L;

	public InvalidRefreshTokenException() {
		super("Invalid or expired refresh token");
	}
}
