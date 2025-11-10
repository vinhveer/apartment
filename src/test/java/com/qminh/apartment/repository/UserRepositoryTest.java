package com.qminh.apartment.repository;

import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest extends PostgresTestContainer {

	@Autowired private UserRepository userRepository;
	@Autowired private RoleRepository roleRepository;

	@Test
	@DisplayName("findByUsername/email returns user with role eagerly loaded")
	void finders_work() {
		Role role = roleRepository.findByRoleName("ADMIN").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("ADMIN"); return roleRepository.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("u1");
		u.setEmail("u1@example.com");
		u.setPassword("x");
		u.setRole(role);
		userRepository.saveAndFlush(u);

		assertThat(userRepository.findByUsername("u1"))
			.isPresent().get().extracting(User::getRole).isNotNull();
		assertThat(userRepository.findByEmail("u1@example.com"))
			.isPresent().get().extracting(User::getRole).isNotNull();
	}
}


