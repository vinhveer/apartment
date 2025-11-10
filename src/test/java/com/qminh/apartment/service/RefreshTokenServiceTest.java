package com.qminh.apartment.service;

import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.service.impl.RefreshTokenService;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RefreshTokenServiceTest extends PostgresTestContainer {

	@Autowired private RefreshTokenService service;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;

	private String username;

	@BeforeEach
	void setupUser() {
		if (roleRepository.findByRoleName("USER").isEmpty()) {
			var r = new Role(); r.setRoleName("USER"); roleRepository.saveAndFlush(r);
		}
		username = "rttest_" + System.nanoTime();
		var role = roleRepository.findByRoleName("USER").orElseThrow();
		User u = new User();
		u.setUsername(username);
		u.setEmail(username + "@example.com");
		u.setPassword("x");
		u.setRole(role);
		userRepository.saveAndFlush(u);
	}

	@Test
	@Transactional
	@DisplayName("storeOrRotate, isValid, rotate, revoke work end-to-end")
	void refresh_token_flow() {
		String t1 = "t1_" + UUID.randomUUID();
		LocalDateTime exp1 = LocalDateTime.now().plusDays(1);
		var saved = service.storeOrRotate(username, t1, exp1);
		assertThat(saved.getToken()).isEqualTo(t1);
		assertThat(service.isValid(t1)).isTrue();

		String t2 = "t2_" + UUID.randomUUID();
		LocalDateTime exp2 = LocalDateTime.now().plusDays(2);
		var rotated = service.rotate(t1, t2, exp2, username);
		assertThat(rotated.getToken()).isEqualTo(t2);
		assertThat(service.isValid(t1)).isFalse();
		assertThat(service.isValid(t2)).isTrue();

		service.revoke(t2);
		assertThat(service.isValid(t2)).isFalse();
	}
}


