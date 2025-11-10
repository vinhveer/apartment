package com.qminh.apartment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.dto.auth.LoginReq;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT extends PostgresTestContainer {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper mapper;
	@Autowired private PasswordEncoder passwordEncoder;
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
				.content(mapper.writeValueAsString(req)))
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
}


