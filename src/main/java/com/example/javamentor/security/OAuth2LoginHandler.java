package com.example.javamentor.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.javamentor.dto.LoginResponse;
import com.example.javamentor.exception.AccountNotRegisteredException;
import com.example.javamentor.exception.EmailAlreadyRegisteredException;
import com.example.javamentor.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2LoginHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
	private final AuthService authService;
	private final String redirectUri;

	public OAuth2LoginHandler(AuthService authService,
			@Value("${app.oauth2.redirect-uri}") String redirectUri) {
		this.authService = authService;
		this.redirectUri = redirectUri;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		OAuth2User user = (OAuth2User) authentication.getPrincipal();
		String email = user.getAttribute("email");
		Boolean emailVerified = user.getAttribute("email_verified");
		boolean registerMode = OAuth2ModeRequestResolver.MODE_REGISTER.equals(readMode(request));
		clearSession(request);

		if (email == null || !Boolean.TRUE.equals(emailVerified)) {
			response.sendRedirect(redirectUri + "#error=email_not_verified");
			return;
		}
		try {
			LoginResponse tokens = authService.loginWithOAuth2(email, user.getAttribute("name"), registerMode);
			response.sendRedirect(redirectUri + "#token=" + tokens.accessToken()
					+ "&refreshToken=" + tokens.refreshToken());
		} catch (EmailAlreadyRegisteredException ex) {
			response.sendRedirect(redirectUri + "#error=already_registered&email="
					+ URLEncoder.encode(email.trim().toLowerCase(), StandardCharsets.UTF_8));
		} catch (AccountNotRegisteredException ex) {
			response.sendRedirect(redirectUri + "#error=not_registered&email="
					+ URLEncoder.encode(ex.getEmail(), StandardCharsets.UTF_8));
		} catch (AuthenticationException ex) {
			response.sendRedirect(redirectUri + "#error=account_disabled");
		}
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		clearSession(request);
		response.sendRedirect(redirectUri + "#error=oauth_failed");
	}

	private String readMode(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		return session == null ? null : (String) session.getAttribute(OAuth2ModeRequestResolver.MODE_ATTRIBUTE);
	}

	private void clearSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}
}
