package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.dto.auth.AuthRes;
import com.qminh.apartment.dto.auth.LoginReq;
import com.qminh.apartment.dto.auth.MessageRes;
import com.qminh.apartment.dto.auth.RefreshRes;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.security.JwtService;
import com.qminh.apartment.service.IRefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final IRefreshTokenService refreshTokenService;
	private final UserRepository userRepository;
	private static final String REFRESH_COOKIE_NAME = "refresh_token";

	private static final long ACCESS_TTL_MS = 10 * 60 * 1000L; // 10 minutes
	private static final long REFRESH_TTL_MS = 14L * 24 * 60 * 60 * 1000; // 14 days

	public AuthController(AuthenticationManager authenticationManager,
	                      JwtService jwtService,
	                      IRefreshTokenService refreshTokenService,
	                      UserRepository userRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
		this.userRepository = userRepository;
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthRes>> login(@Valid @RequestBody LoginReq req, HttpServletResponse response) {
		Authentication auth = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
		);
		String username = auth.getName();
		User user = userRepository.findByUsername(username)
			.orElseGet(() -> userRepository.findByEmail(username).orElseThrow());
		List<String> authorities = List.of("ROLE_" + Objects.requireNonNull(user.getRole()).getRoleName().toUpperCase(Locale.ROOT));
		String access = jwtService.generateAccess(user.getUsername(), authorities, ACCESS_TTL_MS);
		String refresh = jwtService.generateRefresh(user.getUsername(), REFRESH_TTL_MS);
		refreshTokenService.storeOrRotate(user.getUsername(), refresh, LocalDateTime.now().plus(Duration.ofMillis(REFRESH_TTL_MS)));
		setRefreshCookie(response, refresh);

		AuthRes res = new AuthRes();
		res.setAccessToken(access);
		AuthRes.UserInfo info = new AuthRes.UserInfo();
		info.setId(user.getId());
		info.setUsername(user.getUsername());
		info.setRoles(authorities);
		res.setUser(info);
		return ResponseEntity.ok(ApiResponse.ok("Login successfully", res));
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<RefreshRes>> refresh(HttpServletRequest request, HttpServletResponse response) {
		String token = extractRefreshCookie(request);
		if (token == null || !refreshTokenService.isValid(token)) {
			return ResponseEntity.status(401).body(ApiResponse.error("UNAUTHORIZED", "Invalid refresh token", null));
		}
		String username = jwtService.extractUsername(token);
		User user = userRepository.findByUsername(username)
			.orElseGet(() -> userRepository.findByEmail(username).orElseThrow());
		List<String> authorities = List.of("ROLE_" + Objects.requireNonNull(user.getRole()).getRoleName().toUpperCase(Locale.ROOT));
		String newAccess = jwtService.generateAccess(user.getUsername(), authorities, ACCESS_TTL_MS);
		String newRefresh = jwtService.generateRefresh(user.getUsername(), REFRESH_TTL_MS);
		refreshTokenService.rotate(token, newRefresh, LocalDateTime.now().plus(Duration.ofMillis(REFRESH_TTL_MS)), username);
		setRefreshCookie(response, newRefresh);
		RefreshRes res = new RefreshRes();
		res.setAccessToken(newAccess);
		return ResponseEntity.ok(ApiResponse.ok("Refresh successfully", res));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<MessageRes>> logout(HttpServletRequest request, HttpServletResponse response) {
		String token = extractRefreshCookie(request);
		if (token != null) {
			refreshTokenService.revoke(token);
		}
		clearRefreshCookie(response);
		return ResponseEntity.ok(ApiResponse.ok("Logged out", new MessageRes("Logged out")));
	}

	private void setRefreshCookie(HttpServletResponse response, String token) {
		ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, token)
			.httpOnly(true)
			.secure(true)
			.sameSite("Strict")
			.path("/api/auth")
			.maxAge(Duration.ofMillis(REFRESH_TTL_MS))
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private void clearRefreshCookie(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
			.httpOnly(true)
			.secure(true)
			.sameSite("Strict")
			.path("/api/auth")
			.maxAge(0)
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private String extractRefreshCookie(HttpServletRequest request) {
		if (request.getCookies() == null) return null;
		for (var c : request.getCookies()) {
			if (REFRESH_COOKIE_NAME.equals(c.getName())) return c.getValue();
		}
		return null;
	}
}


