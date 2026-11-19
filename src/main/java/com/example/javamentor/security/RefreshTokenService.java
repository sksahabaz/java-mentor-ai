package com.example.javamentor.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.javamentor.entity.RefreshToken;
import com.example.javamentor.entity.Student;
import com.example.javamentor.exception.InvalidRefreshTokenException;
import com.example.javamentor.repository.RefreshTokenRepository;
import com.example.javamentor.repository.StudentRepository;

@Service
public class RefreshTokenService {
	private final SecureRandom secureRandom = new SecureRandom();
	private final RefreshTokenRepository refreshTokenRepository;
	private final StudentRepository studentRepository;
	private final long refreshExpirySeconds;

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, StudentRepository studentRepository,
			@Value("${jwt.refresh-expiry-seconds}") long refreshExpirySeconds) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.studentRepository = studentRepository;
		this.refreshExpirySeconds = refreshExpirySeconds;
	}

	@Transactional
	public String create(Long studentId) {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

		RefreshToken token = new RefreshToken();
		token.setTokenHash(hash(rawToken));
		token.setStudent(studentRepository.getReferenceById(studentId));
		token.setExpiresAt(Instant.now().plusSeconds(refreshExpirySeconds));
		refreshTokenRepository.save(token);
		return rawToken;
	}

	@Transactional(noRollbackFor = AuthenticationException.class)
	public Student consume(String rawToken) {
		RefreshToken token = refreshTokenRepository.findByTokenHash(hash(rawToken))
				.orElseThrow(InvalidRefreshTokenException::new);
		if (token.isRevoked()) {
			refreshTokenRepository.revokeAllByStudentId(token.getStudent().getId());
			throw new InvalidRefreshTokenException();
		}
		if (token.getExpiresAt().isBefore(Instant.now())) {
			throw new InvalidRefreshTokenException();
		}
		token.setRevoked(true);
		return token.getStudent();
	}

	@Transactional
	public void revoke(String rawToken) {
		refreshTokenRepository.findByTokenHash(hash(rawToken))
				.ifPresent(token -> token.setRevoked(true));
	}

	private String hash(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
