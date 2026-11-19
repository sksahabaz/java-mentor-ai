package com.example.javamentor.security;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private final JwtEncoder jwtEncoder;
	private final String issuer;
	private final long expirySeconds;

	public JwtService(JwtEncoder jwtEncoder,
			@Value("${jwt.issuer}") String issuer,
			@Value("${jwt.expiry-seconds}") long expirySeconds) {
		this.jwtEncoder = jwtEncoder;
		this.issuer = issuer;
		this.expirySeconds = expirySeconds;
	}

	public long getExpirySeconds() {
		return expirySeconds;
	}

	public String generateToken(Authentication authentication) {
		Instant now = Instant.now();

		List<String> roles = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.filter(a -> a.startsWith("ROLE_"))
				.toList();

		JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.issuedAt(now)
				.expiresAt(now.plusSeconds(expirySeconds))
				.subject(authentication.getName())
				.claim("roles", roles);

		if (authentication.getPrincipal() instanceof StudentPrincipal principal) {
			claims.claim("studentId", principal.getId());
		}

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build()))
				.getTokenValue();
	}
}
