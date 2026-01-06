package com.qvinh.apartment.features.auth.persistence;

import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.features.auth.domain.RefreshToken;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RefreshTokenRepositoryTest extends PostgresTestContainer {

	@Autowired
	private RefreshTokenRepository repo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private RoleRepository roleRepo;

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

	@Test
	@DisplayName("revokeByUserId revokes all for user, idempotent, no-op on others and not-found")
	void revoke_by_user_id_scenarios() {
		Role r = roleRepo.findAll().stream().findFirst().orElseGet(() -> {
			Role nr = new Role(); nr.setRoleName("USER_" + System.nanoTime()); return roleRepo.saveAndFlush(nr);
		});
		User ua = new User(); ua.setUsername("ua" + System.nanoTime()); ua.setEmail("ua" + System.nanoTime() + "@ex.com"); ua.setPassword("x"); ua.setRole(r);
		User ub = new User(); ub.setUsername("ub" + System.nanoTime()); ub.setEmail("ub" + System.nanoTime() + "@ex.com"); ub.setPassword("x"); ub.setRole(r);
		userRepo.saveAndFlush(ua); userRepo.saveAndFlush(ub);
		// tokens for A: active, already revoked, expired
		String ta1 = "a1_" + UUID.randomUUID(); saveToken(ua, ta1, LocalDateTime.now().plusDays(1), false);
		String ta2 = "a2_" + UUID.randomUUID(); saveToken(ua, ta2, LocalDateTime.now().plusDays(2), true);
		String ta3 = "a3_" + UUID.randomUUID(); saveToken(ua, ta3, LocalDateTime.now().minusMinutes(1), false);
		// token for B
		String tb1 = "b1_" + UUID.randomUUID(); saveToken(ub, tb1, LocalDateTime.now().plusDays(1), false);

		// idempotent and affects only A
		repo.revokeByUserId(java.util.Objects.requireNonNull(ua.getId()));
		repo.revokeByUserId(java.util.Objects.requireNonNull(ua.getId()));
		assertThat(repo.findByToken(ta1)).isPresent().get().extracting(RefreshToken::isRevoked).isEqualTo(true);
		assertThat(repo.findByToken(ta2)).isPresent().get().extracting(RefreshToken::isRevoked).isEqualTo(true);
		assertThat(repo.findByToken(ta3)).isPresent().get().extracting(RefreshToken::isRevoked).isEqualTo(true);
		assertThat(repo.findByToken(tb1)).isPresent().get().extracting(RefreshToken::isRevoked).isEqualTo(false);

		// revoke non-existing token string -> no-op
		repo.revoke("not_exists");
		// revoke unknown user -> no-op
		repo.revokeByUserId(999999L);

		// revoke already revoked token -> still true
		repo.revoke(ta1);
		assertThat(repo.findByToken(ta1)).isPresent().get().extracting(RefreshToken::isRevoked).isEqualTo(true);
	}

	private void saveToken(User user, String token, LocalDateTime exp, boolean revoked) {
		RefreshToken t = new RefreshToken();
		t.setUser(user);
		t.setToken(token);
		t.setExpiresAt(exp);
		t.setRevoked(revoked);
		repo.saveAndFlush(t);
	}
}

