package com.example.javamentor.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.javamentor.entity.Student;

public class StudentPrincipal implements UserDetails {
	private static final long serialVersionUID = 1L;

	private final Student student;

	public StudentPrincipal(Student student) {
		this.student = student;
	}

	public Long getId() {
		return student.getId();
	}

	@Override
	public String getUsername() {
		return student.getEmail();
	}

	@Override
	public String getPassword() {
		return student.getPassword();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return student.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName()))
				.toList();
	}

	@Override
	public boolean isEnabled() {
		return student.isEnabled();
	}
}
