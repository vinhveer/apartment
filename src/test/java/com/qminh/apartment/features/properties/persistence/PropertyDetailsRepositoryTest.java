package com.qminh.apartment.features.properties.persistence;

import com.qminh.apartment.features.accounts.domain.PropertySaleInfo;
import com.qminh.apartment.features.accounts.domain.Role;
import com.qminh.apartment.features.accounts.domain.User;
import com.qminh.apartment.features.accounts.persistence.PropertySaleInfoRepository;
import com.qminh.apartment.features.accounts.persistence.RoleRepository;
import com.qminh.apartment.features.accounts.persistence.UserRepository;
import com.qminh.apartment.features.properties.domain.Property;
import com.qminh.apartment.features.properties.domain.PropertyArea;
import com.qminh.apartment.features.properties.domain.PropertyDefineDetails;
import com.qminh.apartment.features.properties.domain.PropertyDetails;
import com.qminh.apartment.features.properties.domain.PropertyDetailsId;
import com.qminh.apartment.features.properties.domain.PropertyType;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PropertyDetailsRepositoryTest extends PostgresTestContainer {

	@Autowired private PropertyDetailsRepository repository;
	@Autowired private PropertyRepository propertyRepository;
	@Autowired private PropertyDefineDetailsRepository defineRepository;
	@Autowired private PropertyAreaRepository areaRepository;
	@Autowired private PropertyTypeRepository typeRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private PropertySaleInfoRepository saleInfoRepository;

	private Property prepareProperty() {
		PropertyArea area = new PropertyArea();
		area.setAreaName("AreaPD");
		area.setAreaLink("area-pd");
		area = areaRepository.saveAndFlush(area);

		PropertyType type = new PropertyType();
		type.setTypeName("TypePD");
		type = typeRepository.saveAndFlush(type);

		Role saleRole = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role();
			r.setRoleName("SALE");
			return roleRepository.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("salePD");
		u.setEmail("salePD@example.com");
		u.setPassword("x");
		u.setRole(saleRole);
		u = userRepository.saveAndFlush(u);

		PropertySaleInfo info = new PropertySaleInfo();
		info.setUser(u);
		info.setFullName("Sale PD");
		info.setPhone("0900");
		info = saleInfoRepository.saveAndFlush(info);

		Property p = new Property();
		p.setTitle("PD Prop");
		p.setPrice(new BigDecimal("100.00"));
		p.setDescription("d");
		p.setType(type);
		p.setSaleInfo(info);
		p.setArea(area);
		return propertyRepository.saveAndFlush(p);
	}

	private PropertyDefineDetails prepareDefine(String name, boolean isNumber) {
		PropertyDefineDetails d = new PropertyDefineDetails();
		d.setDetailName(name);
		d.setIsNumber(isNumber);
		d.setUnit(isNumber ? "m2" : null);
		d.setShowInHomePage(true);
		return defineRepository.saveAndFlush(d);
	}

	@Test
	@DisplayName("findByProperty, findById_* and count/delete helpers work as expected")
	void repository_helpers_work() {
		Property p = prepareProperty();
		Long pid = Objects.requireNonNull(p.getPropertyId());
		PropertyDefineDetails d1 = prepareDefine("Size", true);
		PropertyDefineDetails d2 = prepareDefine("Interior", false);

		PropertyDetails e1 = new PropertyDetails();
		e1.setId(new PropertyDetailsId(d1.getDetailId(), pid));
		e1.setProperty(p);
		e1.setDetail(d1);
		e1.setValue("100");

		PropertyDetails e2 = new PropertyDetails();
		e2.setId(new PropertyDetailsId(d2.getDetailId(), pid));
		e2.setProperty(p);
		e2.setDetail(d2);
		e2.setValue("Full");

		repository.saveAndFlush(e1);
		repository.saveAndFlush(e2);

		// findByProperty
		var byProp = repository.findByProperty_PropertyId(pid);
		assertThat(byProp).hasSize(2);

		// findById_* helpers
		assertThat(repository.findById_DetailIdAndId_PropertyId(d1.getDetailId(), pid)).isPresent();
		assertThat(repository.findById_DetailIdAndId_PropertyId(999999, pid)).isNotPresent();

		// countById_* for duplicates
		long cnt = repository.countById_PropertyIdAndId_DetailIdIn(pid, List.of(d1.getDetailId(), d2.getDetailId()));
		assertThat(cnt).isEqualTo(2L);

		// delete-by-detail+property
		repository.deleteById_DetailIdAndId_PropertyId(d1.getDetailId(), pid);
		assertThat(repository.findById_DetailIdAndId_PropertyId(d1.getDetailId(), pid)).isNotPresent();
		assertThat(repository.findById_DetailIdAndId_PropertyId(d2.getDetailId(), pid)).isPresent();

		// deleteById_PropertyId
		repository.deleteById_PropertyId(pid);
		assertThat(repository.findByProperty_PropertyId(pid)).isEmpty();
	}
}

