package com.qminh.apartment.features.accounts.persistence;

import com.qminh.apartment.features.accounts.domain.Role;
import com.qminh.apartment.features.accounts.domain.User;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

	@Test
	@DisplayName("findByUsername/email non-existing returns empty")
	void finders_not_found() {
		assertThat(userRepository.findByUsername("unknown_" + System.nanoTime())).isNotPresent();
		assertThat(userRepository.findByEmail("unknown_" + System.nanoTime() + "@ex.com")).isNotPresent();
	}

	@Test
	@DisplayName("searchByRole returns only users with given role and applies keyword filter")
	void search_by_role_with_keyword() {
		Role adminRole = roleRepository.findByRoleName("ADMIN").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("ADMIN"); return roleRepository.saveAndFlush(r);
		});
		Role userRole = roleRepository.findByRoleName("USER").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("USER"); return roleRepository.saveAndFlush(r);
		});

		User u1 = new User();
		u1.setUsername("custA");
		u1.setEmail("custA@example.com");
		u1.setPassword("x");
		u1.setRole(userRole);
		userRepository.saveAndFlush(u1);

		User u2 = new User();
		u2.setUsername("custB");
		u2.setEmail("custB@example.com");
		u2.setPassword("x");
		u2.setRole(userRole);
		userRepository.saveAndFlush(u2);

		User admin = new User();
		admin.setUsername("adminX");
		admin.setEmail("adminX@example.com");
		admin.setPassword("x");
		admin.setRole(adminRole);
		userRepository.saveAndFlush(admin);

		Pageable pageable = PageRequest.of(0, 10);
		Page<User> pageAll = userRepository.searchByRole("USER", null, pageable);
		assertThat(pageAll.getContent()).extracting(User::getRole).allMatch(r -> "USER".equals(r.getRoleName()));

		Page<User> pageFiltered = userRepository.searchByRole("USER", "%custA%", pageable);
		assertThat(pageFiltered.getContent()).extracting(User::getUsername).contains("custA");
		assertThat(pageFiltered.getContent()).extracting(User::getUsername).doesNotContain("custB");
	}

	@Test
	@DisplayName("searchByRoles returns only users with any of given roles and applies keyword filter")
	void search_by_roles_with_keyword() {
		Role adminRole = roleRepository.findByRoleName("ADMIN").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("ADMIN"); return roleRepository.saveAndFlush(r);
		});
		Role saleRole = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("SALE"); return roleRepository.saveAndFlush(r);
		});
		Role userRole = roleRepository.findByRoleName("USER").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("USER"); return roleRepository.saveAndFlush(r);
		});

		User sale = new User();
		sale.setUsername("saleX");
		sale.setEmail("saleX@example.com");
		sale.setPassword("x");
		sale.setRole(saleRole);
		userRepository.saveAndFlush(sale);

		User admin = new User();
		admin.setUsername("adminY");
		admin.setEmail("adminY@example.com");
		admin.setPassword("x");
		admin.setRole(adminRole);
		userRepository.saveAndFlush(admin);

		User user = new User();
		user.setUsername("userZ");
		user.setEmail("userZ@example.com");
		user.setPassword("x");
		user.setRole(userRole);
		userRepository.saveAndFlush(user);

		Pageable pageable = PageRequest.of(0, 10);
		Page<User> pageAll = userRepository.searchByRoles(java.util.List.of("ADMIN", "SALE"), null, pageable);
		assertThat(pageAll.getContent()).extracting(u -> u.getRole().getRoleName())
			.allMatch(rn -> rn.equals("ADMIN") || rn.equals("SALE"));

		Page<User> pageFiltered = userRepository.searchByRoles(java.util.List.of("ADMIN", "SALE"), "%adminY%", pageable);
		assertThat(pageFiltered.getContent()).extracting(User::getUsername).contains("adminY");
		assertThat(pageFiltered.getContent()).extracting(User::getUsername).doesNotContain("saleX");
	}

	@Test
	@DisplayName("avatar field is persisted and loaded correctly")
	void avatar_persisted() {
		Role role = roleRepository.findByRoleName("USER").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("USER"); return roleRepository.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("avatarRepo");
		u.setEmail("avatarRepo@example.com");
		u.setPassword("x");
		u.setRole(role);
		u.setAvatar("data:image/jpeg;base64,xxx");
		userRepository.saveAndFlush(u);

		User found = userRepository.findByUsername("avatarRepo").orElseThrow();
		assertThat(found.getAvatar()).isEqualTo("data:image/jpeg;base64,xxx");
	}
}

