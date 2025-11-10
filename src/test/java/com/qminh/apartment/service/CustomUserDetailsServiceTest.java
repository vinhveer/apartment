package com.qminh.apartment.service;

import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.security.CustomUserDetailsService;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CustomUserDetailsServiceTest extends PostgresTestContainer {

	@Autowired private CustomUserDetailsService service;
	@Autowired private UserRepository userRepository;
	@Autowired private RoleRepository roleRepository;

	private String username;
	private String email;

	@BeforeEach
	void setup() {
		var role = roleRepository.findByRoleName("USER").orElseGet(() -> {
			var r = new Role(); r.setRoleName("USER"); return roleRepository.saveAndFlush(r);
		});
		username = "udstest";
		email = "udstest@example.com";
		if (userRepository.findByUsername(username).isEmpty()) {
			User u = new User();
			u.setUsername(username);
			u.setEmail(email);
			u.setPassword("x");
			u.setRole(role);
			userRepository.saveAndFlush(u);
		}
	}

	@Test
	@DisplayName("loadUserByUsername supports username or email and maps roles")
	void load_user() {
		UserDetails byUsername = service.loadUserByUsername(username);
		assertThat(byUsername.getUsername()).isEqualTo(username);
		assertThat(byUsername.getAuthorities()).extracting("authority").contains("ROLE_USER");
		UserDetails byEmail = service.loadUserByUsername(email);
		assertThat(byEmail.getUsername()).isEqualTo(username);
		assertThat(byEmail.getAuthorities()).extracting("authority").contains("ROLE_USER");
	}
}


