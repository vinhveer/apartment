package com.qminh.apartment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.dto.account.AdminCreateReq;
import com.qminh.apartment.dto.account.SaleCreateReq;
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
class AccountControllerIT extends PostgresTestContainer {

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
		if (userRepository.findByUsername("userz").isEmpty()) {
			Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
			User u = new User();
			u.setUsername("userz");
			u.setEmail("userz@example.com");
			u.setRole(userRole);
			u.setPassword(passwordEncoder.encode("123456"));
			userRepository.saveAndFlush(u);
		}
	}

	private String loginAsRoot() throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername("root");
		req.setPassword("123456");
		var res = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(mapper.writeValueAsString(req)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return mapper.readTree(res).path("data").path("accessToken").asText();
	}

	private String loginAsUser() throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername("userz");
		req.setPassword("123456");
		var res = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(mapper.writeValueAsString(req)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return mapper.readTree(res).path("data").path("accessToken").asText();
	}

	@Test
	@DisplayName("create sale and admin accounts")
	void create_accounts() throws Exception {
		String access = loginAsRoot();

		SaleCreateReq sale = new SaleCreateReq();
		sale.setUsername("sale02");
		sale.setEmail("sale02@example.com");
		sale.setPassword("123456");
		sale.setDisplayName("Sale 02");
		sale.setFullName("Sale Person 02");
		sale.setPhone("0900000002");

		mockMvc.perform(post("/api/create-sale")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(mapper.writeValueAsString(sale)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Create sale account successfully"))
			.andExpect(jsonPath("$.data.username").value("sale02"));

		AdminCreateReq admin = new AdminCreateReq();
		admin.setUsername("admin02");
		admin.setEmail("admin02@example.com");
		admin.setPassword("123456");
		admin.setDisplayName("Admin 02");

		mockMvc.perform(post("/api/create-admin")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(mapper.writeValueAsString(admin)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Create admin account successfully"))
			.andExpect(jsonPath("$.data.username").value("admin02"));
	}

	@Test
	@DisplayName("non-admin cannot create accounts (403)")
	void non_admin_forbidden() throws Exception {
		String access = loginAsUser();
		AdminCreateReq admin = new AdminCreateReq();
		admin.setUsername("admin03");
		admin.setEmail("admin03@example.com");
		admin.setPassword("123456");
		admin.setDisplayName("Admin 03");
		mockMvc.perform(post("/api/create-admin")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(mapper.writeValueAsString(admin)))
			.andExpect(status().isForbidden());
	}
}


