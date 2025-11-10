package com.qminh.apartment.repository;

import com.qminh.apartment.entity.Role;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest extends PostgresTestContainer {

	@Autowired private RoleRepository roleRepository;

	@Test
	@DisplayName("findByRoleName returns role")
	void find_by_name() {
		String name = "ROLE_" + System.nanoTime();
		Role r = new Role();
		r.setRoleName(name);
		roleRepository.saveAndFlush(r);
		assertThat(roleRepository.findByRoleName(name)).isPresent();
	}
}


