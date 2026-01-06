package com.qminh.apartment.features.accounts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.features.accounts.domain.Role;
import com.qminh.apartment.features.accounts.domain.User;
import com.qminh.apartment.features.accounts.persistence.RoleRepository;
import com.qminh.apartment.features.accounts.persistence.UserRepository;
import com.qminh.apartment.features.auth.dto.LoginReq;
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

	@BeforeEach
	void setup() {
		if (roleRepository.findByRoleName("ADMIN").isEmpty()) {
			Role r = new Role(); r.setRoleName("ADMIN"); roleRepository.saveAndFlush(r);
		}
		if (roleRepository.findByRoleName("SALE").isEmpty()) {
			Role r = new Role(); r.setRoleName("SALE"); roleRepository.saveAndFlush(r);
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
		if (userRepository.findByUsername("sale01").isEmpty()) {
			Role sale = roleRepository.findByRoleName("SALE").orElseThrow();
			User u = new User();
			u.setUsername("sale01");
			u.setEmail("sale01@example.com");
			u.setRole(sale);
			u.setPassword(passwordEncoder.encode("123456"));
			userRepository.saveAndFlush(u);
		}
		if (userRepository.findByUsername("user01").isEmpty()) {
			Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
			User u = new User();
			u.setUsername("user01");
			u.setEmail("user01@example.com");
			u.setRole(userRole);
			u.setPassword(passwordEncoder.encode("123456"));
			userRepository.saveAndFlush(u);
		}
	}

	private String login(String username, String password) throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername(username);
		req.setPassword(password);
		var res = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return mapper.readTree(res).path("data").path("accessToken").asText();
	}

	@Test
	@DisplayName("get current user info (ADMIN)")
	void get_me_as_admin() throws Exception {
		String access = login("root", "123456");
		mockMvc.perform(get("/api/users/me")
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Me"))
			.andExpect(jsonPath("$.data.id").exists())
			.andExpect(jsonPath("$.data.username").value("root"))
			.andExpect(jsonPath("$.data.roleName").value("ADMIN"));
	}

	@Test
	@DisplayName("get current user info (SALE)")
	void get_me_as_sale() throws Exception {
		String access = login("sale01", "123456");
		mockMvc.perform(get("/api/users/me")
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Me"))
			.andExpect(jsonPath("$.data.username").value("sale01"))
			.andExpect(jsonPath("$.data.roleName").value("SALE"));
	}

	@Test
	@DisplayName("get current user info (USER)")
	void get_me_as_user() throws Exception {
		String access = login("user01", "123456");
		mockMvc.perform(get("/api/users/me")
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Me"))
			.andExpect(jsonPath("$.data.username").value("user01"))
			.andExpect(jsonPath("$.data.roleName").value("USER"));
	}

	@Test
	@DisplayName("updateMe updates current user's profile fields only")
	void update_me_updates_profile() throws Exception {
		String access = login("user01", "123456");
		String body = "{\"displayName\":\"New Display\",\"fullName\":\"User One\",\"phone\":\"0900999888\"}";
		mockMvc.perform(put("/api/users/me")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update profile successfully"))
			.andExpect(jsonPath("$.data.username").value("user01"))
			.andExpect(jsonPath("$.data.displayName").value("New Display"));
	}

	@Test
	@DisplayName("401 when missing Authorization header")
	void unauthorized_when_no_bearer() throws Exception {
		mockMvc.perform(get("/api/users/me"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("401 when invalid token")
	void unauthorized_when_invalid_token() throws Exception {
		mockMvc.perform(get("/api/users/me")
				.header("Authorization", "Bearer invalid_token"))
			.andExpect(status().isForbidden());
	}
}
