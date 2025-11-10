package com.qminh.apartment.repository;

import com.qminh.apartment.entity.PropertySaleInfo;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PropertySaleInfoRepositoryTest extends PostgresTestContainer {

	@Autowired private PropertySaleInfoRepository saleInfoRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private RoleRepository roleRepository;

	@Test
	@DisplayName("findByUserId and deleteByUserId")
	void find_and_delete() {
		Role role = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("SALE"); return roleRepository.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("saleX" + System.nanoTime());
		u.setEmail("saleX" + System.nanoTime() + "@example.com");
		u.setPassword("x");
		u.setRole(role);
		u = userRepository.saveAndFlush(u);

		PropertySaleInfo info = new PropertySaleInfo();
		info.setUser(u);
		info.setFullName("Sale X");
		info.setPhone("0900000000");
		saleInfoRepository.saveAndFlush(info);

		assertThat(saleInfoRepository.findByUserId(u.getId())).isPresent();
		saleInfoRepository.deleteByUserId(u.getId());
		assertThat(saleInfoRepository.findByUserId(u.getId())).isNotPresent();
	}
}


