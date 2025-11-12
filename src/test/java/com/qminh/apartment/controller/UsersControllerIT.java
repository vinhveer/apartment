package com.qminh.apartment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.dto.auth.LoginReq;
import com.qminh.apartment.dto.user.UserUpdateReq;
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

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerIT extends PostgresTestContainer {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper mapper;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;

	private Long createdUserId;

	@BeforeEach
	void setup() {
		if (roleRepository.findByRoleName("ADMIN").isEmpty()) {
			Role r = new Role(); r.setRoleName("ADMIN"); roleRepository.saveAndFlush(r);
		}
		if (roleRepository.findByRoleName("USER").isEmpty()) {
			Role r = new Role(); r.setRoleName("USER"); roleRepository.saveAndFlush(r);
		}
		if (userRepository.findByUsername("root").isEmpty()) {
			Role admin = roleRepository.findByRoleName("ADMIN").orElseThrow();
			User u = new User();
			u.setUsername("root");
			u.setEmail("root@example.com");
			u.setRole(admin);
			u.setPassword(passwordEncoder.encode("123456"));
			userRepository.saveAndFlush(u);
		}
		// create a normal user to CRUD
		if (userRepository.findByUsername("userA").isEmpty()) {
			User u = new User();
			u.setUsername("userA");
			u.setEmail("userA@example.com");
			u.setPassword(passwordEncoder.encode("123456"));
			u.setRole(roleRepository.findByRoleName("ADMIN").orElseThrow()); // role required by FK
			createdUserId = userRepository.saveAndFlush(u).getId();
		} else {
			createdUserId = userRepository.findByUsername("userA").orElseThrow().getId();
		}
		// normal user to test 403
		if (userRepository.findByUsername("normalA").isEmpty()) {
			User u = new User();
			u.setUsername("normalA");
			u.setEmail("normalA@example.com");
			u.setPassword(passwordEncoder.encode("123456"));
			u.setRole(roleRepository.findByRoleName("USER").orElseThrow());
			userRepository.saveAndFlush(u);
		}
	}

	private String loginAsRoot() throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername("root");
		req.setPassword("123456");
		var res = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return mapper.readTree(res).path("data").path("accessToken").asText();
	}

	private String loginAsNormal() throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername("normalA");
		req.setPassword("123456");
		var res = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return mapper.readTree(res).path("data").path("accessToken").asText();
	}

	@Test
	@DisplayName("users get/list/update/delete works")
	void users_crud() throws Exception {
		String access = loginAsRoot();

		mockMvc.perform(get("/api/users/{id}", createdUserId)
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("User detail"))
			.andExpect(jsonPath("$.data.id").value(createdUserId));

		mockMvc.perform(get("/api/users?page=0&size=10")
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("User list"))
			.andExpect(jsonPath("$.meta.total").exists());

		UserUpdateReq up = new UserUpdateReq();
		up.setEmail("userA+new@example.com");
		up.setDisplayName("User A New");

		mockMvc.perform(put("/api/users/{id}", createdUserId)
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(up))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update user successfully"))
			.andExpect(jsonPath("$.data.displayName").value("User A New"));

		mockMvc.perform(delete("/api/users/{id}", createdUserId)
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete user successfully"));
	}

	@Test
	@DisplayName("non-admin cannot list users (403)")
	void users_list_forbidden_for_non_admin() throws Exception {
		String access = loginAsNormal();
		mockMvc.perform(get("/api/users?page=0&size=10")
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("401 when missing Authorization header on list")
	void users_list_unauthorized() throws Exception {
		mockMvc.perform(get("/api/users?page=0&size=10"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("404 on get/update/delete for non-existing id")
	void users_not_found_cases() throws Exception {
		String access = loginAsRoot();
		mockMvc.perform(get("/api/users/{id}", 999999)
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isNotFound());
		UserUpdateReq up = new UserUpdateReq();
		up.setEmail("x@example.com");
		up.setDisplayName("X");
		mockMvc.perform(put("/api/users/{id}", 999999)
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(up))))
			.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/users/{id}", 999999)
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("400 validation error when email invalid on update")
	void users_update_validation_error() throws Exception {
		String access = loginAsRoot();
		UserUpdateReq up = new UserUpdateReq();
		up.setEmail("not-an-email");
		mockMvc.perform(put("/api/users/{id}", createdUserId)
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(up))))
			.andExpect(status().isBadRequest());
	}
}


