package com.qminh.apartment.repository;

import com.qminh.apartment.entity.RefreshToken;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RefreshTokenRepositoryTest extends PostgresTestContainer {

	@Autowired
	private RefreshTokenRepository repo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private com.qminh.apartment.repository.RoleRepository roleRepo;

	@Test
	@DisplayName("revoke() sets revoked=true")
	void revoke_ok() {
		Role r = roleRepo.findAll().stream().findFirst().orElseGet(() -> {
			Role nr = new Role();
			nr.setRoleName("USER_" + System.nanoTime());
			return roleRepo.saveAndFlush(nr);
		});

		User u = new User();
		u.setUsername("u" + System.nanoTime());
		u.setEmail("u" + System.nanoTime() + "@example.com");
		u.setPassword("x");
		u.setRole(r);
		userRepo.saveAndFlush(u);

		RefreshToken t = new RefreshToken();
		t.setUser(u);
		t.setToken("abc");
		t.setExpiresAt(LocalDateTime.now().plusDays(1));
		t.setRevoked(false);
		repo.saveAndFlush(t);

		repo.revoke("abc");
		assertThat(repo.findByToken("abc")).isPresent().get().extracting(RefreshToken::isRevoked).isEqualTo(true);
	}
}


