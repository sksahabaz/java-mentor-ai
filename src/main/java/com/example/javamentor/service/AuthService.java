package com.example.javamentor.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.javamentor.dto.LoginRequest;
import com.example.javamentor.dto.LoginResponse;
import com.example.javamentor.dto.RefreshRequest;
import com.example.javamentor.dto.RegisterRequest;
import com.example.javamentor.dto.RegisterResponse;
import com.example.javamentor.entity.Role;
import com.example.javamentor.entity.Student;
import com.example.javamentor.exception.AccountNotRegisteredException;
import com.example.javamentor.exception.EmailAlreadyRegisteredException;
import com.example.javamentor.exception.InvalidRefreshTokenException;
import com.example.javamentor.repository.RoleRepository;
import com.example.javamentor.repository.StudentRepository;
import com.example.javamentor.security.JwtService;
import com.example.javamentor.security.RefreshTokenService;
import com.example.javamentor.security.StudentPrincipal;

@Service
public class AuthService {
	private final StudentRepository studentRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;

	public AuthService(StudentRepository studentRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
			JwtService jwtService, RefreshTokenService refreshTokenService) {
		this.studentRepository = studentRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
	}
	@Transactional
	public RegisterResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();

		if (studentRepository.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException(email);
		}

		Role studentRole = roleRepository.findByName(Role.STUDENT)
				.orElseThrow(() -> new IllegalStateException("ROLE_STUDENT missing - did RoleSeeder run?"));

		Student student = new Student();
		student.setName(request.name().trim());
		student.setEmail(email);
		student.setPhone(request.phone());
		student.setCourse(request.course());
		student.setLevel(request.level());
		student.setPassword(passwordEncoder.encode(request.password()));
		student.setEnabled(true);
		student.addRole(studentRole);

		Student saved = studentRepository.save(student);

		return new RegisterResponse(saved.getId(), saved.getEmail(), "Registration successful");
	}

	public LoginResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
		return issueTokens((StudentPrincipal) authentication.getPrincipal());
	}

	@Transactional(noRollbackFor = AuthenticationException.class)
	public LoginResponse refresh(RefreshRequest request) {
		Student student = refreshTokenService.consume(request.refreshToken());
		if (!student.isEnabled()) {
			throw new InvalidRefreshTokenException();
		}
		return issueTokens(new StudentPrincipal(student));
	}

	public void logout(RefreshRequest request) {
		refreshTokenService.revoke(request.refreshToken());
	}

	private LoginResponse issueTokens(StudentPrincipal principal) {
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
				principal, null, principal.getAuthorities());
		String accessToken = jwtService.generateToken(authentication);
		String refreshToken = refreshTokenService.create(principal.getId());
		return new LoginResponse(accessToken, "Bearer", jwtService.getExpirySeconds(), refreshToken);
	}

	@Transactional
	public LoginResponse loginWithOAuth2(String email, String name, boolean registerMode) {
		String normalized = email.trim().toLowerCase();
		Student student;
		if (registerMode) {
			if (studentRepository.existsByEmail(normalized)) {
				throw new EmailAlreadyRegisteredException(normalized);
			}
			student = createOAuth2Student(normalized, name);
		} else {
			student = studentRepository.findByEmail(normalized)
					.orElseThrow(() -> new AccountNotRegisteredException(normalized));
		}
		if (!student.isEnabled()) {
			throw new DisabledException("Account disabled");
		}
		return issueTokens(new StudentPrincipal(student));
	}

	private Student createOAuth2Student(String email, String name) {
		Role studentRole = roleRepository.findByName(Role.STUDENT)
				.orElseThrow(() -> new IllegalStateException("ROLE_STUDENT missing"));
		Student student = new Student();
		student.setName((name == null || name.isBlank()) ? email : name.trim());
		student.setEmail(email);
		student.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
		student.setEnabled(true);
		student.addRole(studentRole);
		return studentRepository.save(student);
	}
}
