package com.qminh.apartment.features.accounts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.features.accounts.domain.Role;
import com.qminh.apartment.features.accounts.domain.User;
import com.qminh.apartment.features.accounts.dto.account.AccountCreateReq;
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
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
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
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return mapper.readTree(res).path("data").path("accessToken").asText();
	}

	@Test
	@DisplayName("create employee accounts via unified endpoint")
	void create_accounts() throws Exception {
		String access = loginAsRoot();

		AccountCreateReq sale = new AccountCreateReq();
		sale.setUsername("sale02");
		sale.setEmail("sale02@example.com");
		sale.setPassword("123456");
		sale.setDisplayName("Sale 02");
		sale.setFullName("Sale Person 02");
		sale.setPhone("0900000002");
		sale.setRoleName("SALE");

		mockMvc.perform(post("/api/create-employee-account")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(sale))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Create employee account successfully"))
			.andExpect(jsonPath("$.data.username").value("sale02"));

		AccountCreateReq admin = new AccountCreateReq();
		admin.setUsername("admin02");
		admin.setEmail("admin02@example.com");
		admin.setPassword("123456");
		admin.setDisplayName("Admin 02");
		admin.setRoleName("ADMIN");

		mockMvc.perform(post("/api/create-employee-account")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(admin))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Create employee account successfully"))
			.andExpect(jsonPath("$.data.username").value("admin02"));
	}

	@Test
	@DisplayName("non-admin cannot create employee accounts (403)")
	void non_admin_forbidden() throws Exception {
		String access = loginAsUser();
		AccountCreateReq admin = new AccountCreateReq();
		admin.setUsername("admin03");
		admin.setEmail("admin03@example.com");
		admin.setPassword("123456");
		admin.setDisplayName("Admin 03");
		admin.setRoleName("ADMIN");
		mockMvc.perform(post("/api/create-employee-account")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(admin))))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("401 when missing Authorization header")
	void unauthorized_when_no_bearer() throws Exception {
		AccountCreateReq sale = new AccountCreateReq();
		sale.setUsername("sale401");
		sale.setEmail("sale401@example.com");
		sale.setPassword("123456");
		sale.setDisplayName("Sale 401");
		sale.setFullName("S401");
		sale.setPhone("0900");
		sale.setRoleName("SALE");
		mockMvc.perform(post("/api/create-employee-account")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(sale))))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("400 validation error when required fields missing")
	void create_sale_validation_error() throws Exception {
		String access = loginAsRoot();
		AccountCreateReq sale = new AccountCreateReq(); // missing all fields
		mockMvc.perform(post("/api/create-employee-account")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(sale))))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("409 conflict when creating duplicate username/email")
	void conflict_on_duplicate() throws Exception {
		String access = loginAsRoot();
		AccountCreateReq sale = new AccountCreateReq();
		sale.setUsername("dupUser");
		sale.setEmail("dup@example.com");
		sale.setPassword("123456");
		sale.setDisplayName("Dup");
		sale.setFullName("Dup Full");
		sale.setPhone("0900");
		sale.setRoleName("SALE");
		mockMvc.perform(post("/api/create-employee-account")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(sale))))
			.andExpect(status().isOk());
		// duplicate
		mockMvc.perform(post("/api/create-employee-account")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(sale))))
			.andExpect(status().isConflict());
	}
}

