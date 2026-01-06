package com.qminh.apartment.features.auth.application;

import com.qminh.apartment.features.accounts.domain.Role;
import com.qminh.apartment.features.accounts.domain.User;
import com.qminh.apartment.features.accounts.persistence.RoleRepository;
import com.qminh.apartment.features.accounts.persistence.UserRepository;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RefreshTokenServiceTest extends PostgresTestContainer {

	@Autowired private RefreshTokenService service;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;

	private String username;
	private String otherUsername;

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

		otherUsername = "rttest_other_" + System.nanoTime();
		User o = new User();
		o.setUsername(otherUsername);
		o.setEmail(otherUsername + "@example.com");
		o.setPassword("x");
		o.setRole(role);
		userRepository.saveAndFlush(o);
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

	@Test
	@Transactional
	@DisplayName("rotate fails with 403 (AccessDeniedException) when token not owned by username")
	void rotate_fails_when_not_owner() {
		String t1 = "own_" + UUID.randomUUID();
		LocalDateTime exp = LocalDateTime.now().plusDays(1);
		service.storeOrRotate(username, t1, exp);
		String newTok = "new_" + UUID.randomUUID();
		LocalDateTime newExp = LocalDateTime.now().plusDays(2);
		assertThatThrownBy(() -> service.rotate(t1, newTok, newExp, otherUsername))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@Transactional
	@DisplayName("rotate fails with 400 (IllegalArgumentException) when token expired or revoked")
	void rotate_fails_when_expired_or_revoked() {
		// expired case
		String tExpired = "exp_" + UUID.randomUUID();
		service.storeOrRotate(username, tExpired, LocalDateTime.now().minusMinutes(1));
		String n1 = "n1_" + UUID.randomUUID();
		LocalDateTime exp1 = LocalDateTime.now().plusDays(1);
		assertThatThrownBy(() -> service.rotate(tExpired, n1, exp1, username))
			.isInstanceOf(IllegalArgumentException.class);
		// revoked case
		String tValid = "valid_" + UUID.randomUUID();
		service.storeOrRotate(username, tValid, LocalDateTime.now().plusDays(1));
		service.revoke(tValid);
		String n2 = "n2_" + UUID.randomUUID();
		LocalDateTime exp2 = LocalDateTime.now().plusDays(1);
		assertThatThrownBy(() -> service.rotate(tValid, n2, exp2, username))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@Transactional
	@DisplayName("isValid returns false for blank, nonexistent, and expired tokens")
	void is_valid_edge_cases() {
		assertThat(service.isValid("")).isFalse();
		assertThat(service.isValid("does_not_exist")).isFalse();
		String t = "soon_" + UUID.randomUUID();
		service.storeOrRotate(username, t, LocalDateTime.now().minusMinutes(1));
		assertThat(service.isValid(t)).isFalse();
	}
}

