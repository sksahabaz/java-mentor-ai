package com.example.javamentor.security;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class OAuth2ModeRequestResolver implements OAuth2AuthorizationRequestResolver {
	public static final String MODE_ATTRIBUTE = "OAUTH2_MODE";
	public static final String MODE_REGISTER = "register";
	public static final String MODE_LOGIN = "login";

	private final OAuth2AuthorizationRequestResolver delegate;

	public OAuth2ModeRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
		this.delegate = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
				"/oauth2/authorization");
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		return rememberMode(request, delegate.resolve(request));
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
		return rememberMode(request, delegate.resolve(request, clientRegistrationId));
	}

	private OAuth2AuthorizationRequest rememberMode(HttpServletRequest request,
			OAuth2AuthorizationRequest authorizationRequest) {
		if (authorizationRequest != null) {
			String mode = MODE_REGISTER.equals(request.getParameter("mode")) ? MODE_REGISTER : MODE_LOGIN;
			request.getSession().setAttribute(MODE_ATTRIBUTE, mode);
		}
		return authorizationRequest;
	}
}
