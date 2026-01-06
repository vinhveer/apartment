package com.qvinh.apartment.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.features.auth.dto.LoginReq;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import java.util.Locale;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("contract")
public abstract class ContractTestBase extends PostgresTestContainer {

	protected static final String AUTH = "Authorization";
	protected static final String BEARER = "Bearer ";

	@Autowired protected MockMvc mockMvc;
	@Autowired protected ObjectMapper objectMapper;
	@Autowired protected PasswordEncoder passwordEncoder;
	@Autowired protected RoleRepository roleRepository;
	@Autowired protected UserRepository userRepository;

	@BeforeEach
	void seedUsers() {
		ensureRole("ADMIN");
		ensureRole("USER");
		ensureRole("SALE");
		ensureUser("root", "root@example.com", "ADMIN", "123456");
		ensureUser("user01", "user01@example.com", "USER", "123456");
	}

	protected void ensureRole(String roleName) {
		roleRepository.findByRoleName(roleName.toUpperCase(Locale.ROOT)).orElseGet(() -> {
			Role r = new Role();
			r.setRoleName(roleName.toUpperCase(Locale.ROOT));
			return roleRepository.saveAndFlush(r);
		});
	}

	protected void ensureUser(String username, String email, String roleName, String rawPassword) {
		userRepository.findByUsername(username).orElseGet(() -> {
			Role role = roleRepository.findByRoleName(roleName.toUpperCase(Locale.ROOT)).orElseThrow();
			User u = new User();
			u.setUsername(username);
			u.setEmail(email);
			u.setRole(role);
			u.setPassword(passwordEncoder.encode(rawPassword));
			return userRepository.saveAndFlush(u);
		});
	}

	protected String login(String username, String password) throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername(username);
		req.setPassword(password);
		String res = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(objectMapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(res).path("data").path("accessToken").asText();
	}
}
