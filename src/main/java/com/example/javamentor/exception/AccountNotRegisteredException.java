package com.example.javamentor.exception;

public class AccountNotRegisteredException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final String email;

	public AccountNotRegisteredException(String email) {
		super("No account registered for " + email);
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
}
