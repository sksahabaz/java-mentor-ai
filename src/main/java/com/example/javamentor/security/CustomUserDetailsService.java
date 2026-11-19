package com.example.javamentor.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.javamentor.repository.StudentRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	private final StudentRepository studentRepository;

	public CustomUserDetailsService(StudentRepository studentRepository) {
		this.studentRepository = studentRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String email = username.trim().toLowerCase();
		return studentRepository.findByEmail(email)
				.map(StudentPrincipal::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}
