package com.qvinh.apartment.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	public JwtAuthFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
		throws ServletException, IOException {
		String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (auth != null && auth.startsWith("Bearer ")) {
			String token = auth.substring(7);
			try {
				String username = jwtService.extractUsername(token);
				var authorities = jwtService.extractAuthorities(token).stream()
					.map(SimpleGrantedAuthority::new).toList();
				var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (Exception ignored) {
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}
}

