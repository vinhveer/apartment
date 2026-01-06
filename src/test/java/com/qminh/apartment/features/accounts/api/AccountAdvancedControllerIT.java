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
class AccountAdvancedControllerIT extends PostgresTestContainer {

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
	@DisplayName("GET/PUT/DELETE employee accounts and change role works")
	void employee_accounts_and_role_flow() throws Exception {
		String access = loginAsRoot();

		// create SALE employee via API
		AccountCreateReq sale = new AccountCreateReq();
		sale.setUsername("saleIT");
		sale.setEmail("saleIT@example.com");
		sale.setPassword("123456");
		sale.setDisplayName("Sale IT");
		sale.setFullName("Sale IT Full");
		sale.setPhone("0900");
		sale.setRoleName("SALE");
		mockMvc.perform(post("/api/create-employee-account")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(sale))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.username").value("saleIT"));

		// list employees
		mockMvc.perform(get("/api/accounts/employees?page=0&size=10&q=saleIT")
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Employee account list"))
			.andExpect(jsonPath("$.data.content[0].username").value("saleIT"));

		// edit employee
		var emp = userRepository.findByUsername("saleIT").orElseThrow();
		var up = """
			{"email":"saleIT+new@example.com","displayName":"Sale IT New"}
			""";
		mockMvc.perform(put("/api/accounts/employees/{id}", emp.getId())
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(up))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update employee account successfully"))
			.andExpect(jsonPath("$.data.email").value("saleIT+new@example.com"))
			.andExpect(jsonPath("$.data.displayName").value("Sale IT New"));

		// change role SALE -> ADMIN (sale info kept)
		var toAdmin = """
			{"roleName":"ADMIN"}
			""";
		mockMvc.perform(put("/api/accounts/employees/{id}/role", emp.getId())
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(toAdmin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update employee role successfully"))
			.andExpect(jsonPath("$.data.roleName").value("ADMIN"));

		// change role ADMIN -> SALE without fullName/phone (should auto-backfill)
		var toSale = """
			{"roleName":"SALE"}
			""";
		mockMvc.perform(put("/api/accounts/employees/{id}/role", emp.getId())
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(toSale))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update employee role successfully"))
			.andExpect(jsonPath("$.data.roleName").value("SALE"));

		// delete employee
		mockMvc.perform(delete("/api/accounts/employees/{id}", emp.getId())
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete employee account successfully"));
	}
}

