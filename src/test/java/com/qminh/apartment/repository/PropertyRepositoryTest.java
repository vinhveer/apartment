package com.qminh.apartment.repository;

import com.qminh.apartment.entity.*;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PropertyRepositoryTest extends PostgresTestContainer {

	@Autowired private PropertyRepository propertyRepository;
	@Autowired private PropertyTypeRepository typeRepository;
	@Autowired private PropertyAreaRepository areaRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private PropertySaleInfoRepository saleInfoRepository;

	@Test
	@DisplayName("save() persists property with relations and generates id")
	void save_ok() {
		PropertyType type = new PropertyType(); type.setTypeName("T" + System.nanoTime());
		type = typeRepository.saveAndFlush(type);
		PropertyArea area = new PropertyArea(); area.setAreaName("N" + System.nanoTime()); area.setAreaLink("L" + System.nanoTime());
		area = areaRepository.saveAndFlush(area);
		Role role = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("SALE"); return roleRepository.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("s" + System.nanoTime());
		u.setEmail("s" + System.nanoTime() + "@ex.com");
		u.setPassword("x");
		u.setRole(role);
		u = userRepository.saveAndFlush(u);
		PropertySaleInfo si = new PropertySaleInfo();
		si.setUser(u);
		si.setFullName("FN");
		si.setPhone("0900");
		saleInfoRepository.saveAndFlush(si);

		Property p = new Property();
		p.setTitle("P1");
		p.setPrice(new BigDecimal("123.45"));
		p.setDescription("d");
		p.setType(type);
		p.setSaleInfo(si);
		p.setArea(area);
		p.setIsPublic(Boolean.TRUE);

		Property saved = propertyRepository.saveAndFlush(p);
		assertThat(Objects.requireNonNull(saved.getPropertyId())).isNotNull();
		assertThat(propertyRepository.findById(saved.getPropertyId())).isPresent();
	}
}


