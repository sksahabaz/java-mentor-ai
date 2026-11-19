package com.example.javamentor.security;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {
	private CurrentUser() {
	}

	public static Long studentId(Jwt jwt) {
		Object value = jwt.getClaim("studentId");
		if (value instanceof Number number) {
			return number.longValue();
		}
		throw new InsufficientAuthenticationException("Token has no studentId claim");
	}
}
