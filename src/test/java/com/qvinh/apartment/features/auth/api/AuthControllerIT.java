package com.qvinh.apartment.features.auth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.features.auth.dto.LoginReq;
import com.qvinh.apartment.features.auth.persistence.RefreshTokenRepository;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT extends PostgresTestContainer {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper mapper;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private RefreshTokenRepository refreshTokenRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;

	@BeforeEach
	void setup() {
		if (roleRepository.findAll().isEmpty()) {
			Role r = new Role(); r.setRoleName("USER"); roleRepository.save(r);
		}
		if (userRepository.findByUsername("vinh").isEmpty()) {
			Role r = roleRepository.findAll().getFirst();
			User u = new User();
			u.setUsername("vinh");
			u.setEmail("vinh@example.com");
			u.setRole(r);
			u.setPassword(passwordEncoder.encode("123456"));
			userRepository.save(u);
		}
	}

	@Test
	@DisplayName("login -> refresh -> me -> logout works")
	void auth_flow() throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername("vinh");
		req.setPassword("123456");

		var loginRes = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(cookie().exists("refresh_token"))
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse();

		String access = mapper.readTree(loginRes.getContentAsString()).path("data").path("accessToken").asText();

		mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.username").value("vinh"));

		mockMvc.perform(post("/api/auth/refresh").cookie(loginRes.getCookies()))
			.andExpect(status().isOk())
			.andExpect(cookie().exists("refresh_token"))
			.andExpect(jsonPath("$.data.accessToken").exists());

		mockMvc.perform(post("/api/auth/logout").cookie(loginRes.getCookies()))
			.andExpect(status().isOk())
			.andExpect(cookie().value("refresh_token", ""));
	}

	@Test
	@DisplayName("login fails with 401 for wrong credentials; refresh fails without cookie; cookie attributes set")
	void auth_negative_and_cookie_attrs() throws Exception {
		LoginReq bad = new LoginReq();
		bad.setUsername("vinh");
		bad.setPassword("wrong");
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(bad))))
			.andExpect(status().isUnauthorized());
		// refresh without cookie
		mockMvc.perform(post("/api/auth/refresh"))
			.andExpect(status().isUnauthorized());
		// login ok and assert cookie flags
		LoginReq ok = new LoginReq();
		ok.setUsername("vinh");
		ok.setPassword("123456");
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(ok))))
			.andExpect(status().isOk())
			.andExpect(header().stringValues("Set-Cookie", Objects.requireNonNull(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("HttpOnly")))))
			.andExpect(header().stringValues("Set-Cookie", Objects.requireNonNull(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("SameSite=Strict")))))
			.andExpect(header().stringValues("Set-Cookie", Objects.requireNonNull(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Path=/api/auth")))));
		// logout without cookie is OK (idempotent)
		mockMvc.perform(post("/api/auth/logout"))
			.andExpect(status().isOk());
	}
	@Test
	@DisplayName("login twice keeps only one active refresh token for user")
	void login_twice_single_active_refresh_token() throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername("vinh");
		req.setPassword("123456");
		// first login
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(cookie().exists("refresh_token"));
		// second login
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(cookie().exists("refresh_token"));
		var user = userRepository.findByUsername("vinh").orElseThrow();
		long active = refreshTokenRepository.findAll().stream()
			.filter(rt -> rt.getUser() != null && rt.getUser().getId().equals(user.getId()))
			.filter(rt -> !rt.isRevoked())
			.count();
		org.assertj.core.api.Assertions.assertThat(active).isEqualTo(1);
	}
}

