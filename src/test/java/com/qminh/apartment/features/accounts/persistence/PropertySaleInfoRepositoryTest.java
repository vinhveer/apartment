package com.qminh.apartment.features.accounts.persistence;

import com.qminh.apartment.features.accounts.domain.PropertySaleInfo;
import com.qminh.apartment.features.accounts.domain.Role;
import com.qminh.apartment.features.accounts.domain.User;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

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

		assertThat(saleInfoRepository.findByUserId(java.util.Objects.requireNonNull(u.getId()))).isPresent();
		saleInfoRepository.deleteByUserId(java.util.Objects.requireNonNull(u.getId()));
		assertThat(saleInfoRepository.findByUserId(java.util.Objects.requireNonNull(u.getId()))).isNotPresent();
	}

	@Test
	@DisplayName("findByUserId empty when user has no sale info; deleteByUserId unknown is no-op")
	void empty_and_noop() {
		Role role = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("SALE"); return roleRepository.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("saleY" + System.nanoTime());
		u.setEmail("saleY" + System.nanoTime() + "@example.com");
		u.setPassword("x");
		u.setRole(role);
		u = userRepository.saveAndFlush(u);
		assertThat(saleInfoRepository.findByUserId(java.util.Objects.requireNonNull(u.getId()))).isNotPresent();
		// no-op
		saleInfoRepository.deleteByUserId(999999L);
	}

	@Test
	@DisplayName("cannot create second sale info for same user (one-to-one)")
	void duplicate_for_same_user_violates() {
		Role role = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("SALE"); return roleRepository.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("saleZ" + System.nanoTime());
		u.setEmail("saleZ" + System.nanoTime() + "@example.com");
		u.setPassword("x");
		u.setRole(role);
		u = userRepository.saveAndFlush(u);
		PropertySaleInfo info1 = new PropertySaleInfo();
		info1.setUser(u);
		info1.setFullName("Sale Z");
		info1.setPhone("0900");
		saleInfoRepository.saveAndFlush(info1);
		PropertySaleInfo info2 = new PropertySaleInfo();
		info2.setUser(u);
		info2.setFullName("Sale Z2");
		info2.setPhone("0901");
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> saleInfoRepository.saveAndFlush(info2))
			.isInstanceOf(DataIntegrityViolationException.class);
	}
}

