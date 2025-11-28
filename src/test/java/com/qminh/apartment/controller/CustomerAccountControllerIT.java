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
class CustomerAccountControllerIT extends PostgresTestContainer {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper mapper;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;

	private Long customerId;

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
		if (userRepository.findByUsername("customer01").isEmpty()) {
			Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
			User u = new User();
			u.setUsername("customer01");
			u.setEmail("customer01@example.com");
			u.setDisplayName("Customer 01");
			u.setRole(userRole);
			u.setPassword(passwordEncoder.encode("123456"));
			userRepository.saveAndFlush(u);
			customerId = u.getId();
		} else {
			customerId = userRepository.findByUsername("customer01").orElseThrow().getId();
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

	@Test
	@DisplayName("get customer accounts list (ADMIN)")
	void get_customer_accounts() throws Exception {
		String access = loginAsRoot();
		mockMvc.perform(get("/api/accounts/customers")
				.header("Authorization", "Bearer " + access)
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Customer account list"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.meta").exists());
	}

	@Test
	@DisplayName("search customer accounts with query (ADMIN)")
	void search_customer_accounts() throws Exception {
		String access = loginAsRoot();
		mockMvc.perform(get("/api/accounts/customers")
				.header("Authorization", "Bearer " + access)
				.param("page", "0")
				.param("size", "10")
				.param("q", "customer"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Customer account list"))
			.andExpect(jsonPath("$.data.content").isArray());
	}

	@Test
	@DisplayName("edit customer account (ADMIN)")
	void edit_customer_account() throws Exception {
		String access = loginAsRoot();
		UserUpdateReq req = new UserUpdateReq();
		req.setEmail("customer01+new@example.com");
		req.setDisplayName("Customer 01 Updated");
		mockMvc.perform(put("/api/accounts/customers/" + customerId)
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update customer account successfully"))
			.andExpect(jsonPath("$.data.email").value("customer01+new@example.com"))
			.andExpect(jsonPath("$.data.displayName").value("Customer 01 Updated"));
	}

	@Test
	@DisplayName("delete customer account (ADMIN)")
	void delete_customer_account() throws Exception {
		String access = loginAsRoot();
		Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
		User tempCustomer = new User();
		tempCustomer.setUsername("temp_customer");
		tempCustomer.setEmail("temp@example.com");
		tempCustomer.setRole(userRole);
		tempCustomer.setPassword(passwordEncoder.encode("123456"));
		userRepository.saveAndFlush(tempCustomer);
		Long tempId = tempCustomer.getId();
		mockMvc.perform(delete("/api/accounts/customers/" + tempId)
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete customer account successfully"));
	}

	@Test
	@DisplayName("403 forbidden when non-admin tries to access")
	void non_admin_forbidden() throws Exception {
		Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
		User nonAdmin = new User();
		nonAdmin.setUsername("nonadmin");
		nonAdmin.setEmail("nonadmin@example.com");
		nonAdmin.setRole(userRole);
		nonAdmin.setPassword(passwordEncoder.encode("123456"));
		userRepository.saveAndFlush(nonAdmin);
		LoginReq loginReq = new LoginReq();
		loginReq.setUsername("nonadmin");
		loginReq.setPassword("123456");
		var loginRes = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(loginReq))))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		String access = mapper.readTree(loginRes).path("data").path("accessToken").asText();
		mockMvc.perform(get("/api/accounts/customers")
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("404 when customer account not found")
	void customer_not_found() throws Exception {
		String access = loginAsRoot();
		mockMvc.perform(put("/api/accounts/customers/99999")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(new UserUpdateReq()))))
			.andExpect(status().isNotFound());
	}
}

