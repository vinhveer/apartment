package com.qvinh.apartment.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvinh.apartment.ApartmentApplication;
import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.features.auth.dto.LoginReq;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Import;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ApartmentApplication.class)
@Import(ErrorContractIT.TestBoomControllerConfig.class)
@AutoConfigureMockMvc
class ErrorContractIT extends PostgresTestContainer {

	@Autowired private MockMvc mockMvc;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;

	private final ObjectMapper mapper = new ObjectMapper();
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@BeforeEach
	void setup() {
		ensureRole("ADMIN");
		ensureRole("SALE");
		ensureRole("USER");

		ensureUser("root", "root@example.com", "ADMIN");
		ensureUser("sale01", "sale01@example.com", "SALE");
	}

	private void ensureRole(String roleName) {
		roleRepository.findByRoleName(roleName).orElseGet(() -> {
			Role r = new Role();
			r.setRoleName(roleName);
			return roleRepository.saveAndFlush(r);
		});
	}

	private void ensureUser(String username, String email, String roleName) {
		userRepository.findByUsername(username).orElseGet(() -> {
			Role role = roleRepository.findByRoleName(roleName).orElseThrow();
			User u = new User();
			u.setUsername(username);
			u.setEmail(email);
			u.setRole(role);
			u.setPassword(passwordEncoder.encode("123456"));
			return userRepository.saveAndFlush(u);
		});
	}

	private String login(String username) throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername(username);
		req.setPassword("123456");
		var res = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return mapper.readTree(res).path("data").path("accessToken").asText();
	}

	private void assertRequestIdMatches(String body, String headerId) throws Exception {
		assertThat(headerId).isNotBlank();
		String bodyId = mapper.readTree(body).path("error").path("requestId").asText();
		assertThat(bodyId).isEqualTo(headerId);
	}

	@Test
	@DisplayName("401 JSON on protected endpoint with no token")
	void unauthorized_json() throws Exception {
		var resp = mockMvc.perform(get("/api/users/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
			.andExpect(jsonPath("$.error.message").isNotEmpty())
			.andExpect(jsonPath("$.error.requestId").isNotEmpty())
			.andReturn().getResponse();

		assertRequestIdMatches(resp.getContentAsString(), resp.getHeader("X-Request-Id"));
	}

	@Test
	@DisplayName("403 JSON on endpoint with insufficient role")
	void forbidden_json() throws Exception {
		String access = login("sale01");
		var resp = mockMvc.perform(get("/api/accounts/customers")
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isForbidden())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
			.andExpect(jsonPath("$.error.requestId").isNotEmpty())
			.andReturn().getResponse();

		assertRequestIdMatches(resp.getContentAsString(), resp.getHeader("X-Request-Id"));
	}

	@Test
	@DisplayName("422 validation with fieldErrors")
	void validation_422() throws Exception {
		String access = login("root");
		String requestId = "rid-validation-1";

		var resp = mockMvc.perform(post("/api/create-employee-account")
				.header("Authorization", "Bearer " + access)
				.header("X-Request-Id", requestId)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{}"))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.error.fieldErrors").isArray())
			.andExpect(jsonPath("$.error.fieldErrors[0].field").isNotEmpty())
			.andExpect(jsonPath("$.error.fieldErrors[0].message").isNotEmpty())
			.andExpect(jsonPath("$.error.requestId").value(requestId))
			.andReturn().getResponse();

		assertThat(resp.getHeader("X-Request-Id")).isEqualTo(requestId);
	}

	@Test
	@DisplayName("404 domain not found -> USER_NOT_FOUND")
	void not_found_404() throws Exception {
		String access = login("root");
		var resp = mockMvc.perform(put("/api/accounts/employees/999999")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{\"displayName\":\"x\"}"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"))
			.andExpect(jsonPath("$.error.requestId").isNotEmpty())
			.andReturn().getResponse();

		assertRequestIdMatches(resp.getContentAsString(), resp.getHeader("X-Request-Id"));
	}

	@Test
	@DisplayName("409 conflict -> USERNAME_ALREADY_EXISTS")
	void conflict_409() throws Exception {
		String access = login("root");
		String unique = "dupUser" + System.nanoTime();
		String body = """
			{
			  "username": "%s",
			  "email": "%s@example.com",
			  "password": "123456",
			  "displayName": "Dup User",
			  "fullName": "Dup User Full",
			  "phone": "0900",
			  "roleName": "SALE"
			}
			""".formatted(unique, unique);

		mockMvc.perform(post("/api/create-employee-account")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andExpect(status().isOk());

		var resp = mockMvc.perform(post("/api/create-employee-account")
				.header("Authorization", "Bearer " + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andExpect(status().isConflict())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("USERNAME_ALREADY_EXISTS"))
			.andExpect(jsonPath("$.error.requestId").isNotEmpty())
			.andReturn().getResponse();

		assertRequestIdMatches(resp.getContentAsString(), resp.getHeader("X-Request-Id"));
	}

	@Test
	@DisplayName("500 fallback internal error -> INTERNAL_ERROR")
	void internal_500() throws Exception {
		String access = login("root");
		var resp = mockMvc.perform(get("/api/test/boom")
				.header("Authorization", "Bearer " + access))
			.andExpect(status().isInternalServerError())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
			.andExpect(jsonPath("$.error.requestId").isNotEmpty())
			.andReturn().getResponse();

		assertRequestIdMatches(resp.getContentAsString(), resp.getHeader("X-Request-Id"));
	}

	@Configuration
	static class TestBoomControllerConfig {
		@Bean
		BoomController boomController() {
			return new BoomController();
		}
	}

	@RestController
	static class BoomController {
		@GetMapping("/api/test/boom")
		public String boom() {
			throw new RuntimeException("boom");
		}
	}
}
