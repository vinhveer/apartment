package com.qvinh.apartment.features.properties.application;

import com.qvinh.apartment.features.accounts.domain.*;
import com.qvinh.apartment.features.accounts.persistence.*;
import com.qvinh.apartment.features.properties.domain.*;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsCreateReq;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsItemReq;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsUpdateReq;
import com.qvinh.apartment.features.properties.persistence.*;
import com.qvinh.apartment.shared.exception.BusinessException;
import com.qvinh.apartment.shared.exception.ConflictException;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PropertyDetailsServiceTest extends PostgresTestContainer {

	@Autowired private PropertyDetailsService service;
	@Autowired private PropertyRepository propertyRepository;
	@Autowired private PropertyDefineDetailsRepository defineRepository;
	@Autowired private PropertyDetailsRepository detailsRepository;
	@Autowired private PropertyAreaRepository areaRepository;
	@Autowired private PropertyTypeRepository typeRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private PropertySaleInfoRepository saleInfoRepository;

	private Long propertyId;
	private Integer numericDetailId;
	private Integer textDetailId;

	@BeforeEach
	void setup() {
		PropertyArea area = new PropertyArea();
		area.setAreaName("AreaPD-S");
		area.setAreaLink("area-pd-s");
		area = areaRepository.saveAndFlush(area);

		PropertyType type = new PropertyType();
		type.setTypeName("TypePD-S");
		type = typeRepository.saveAndFlush(type);

		Role saleRole = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role();
			r.setRoleName("SALE");
			return roleRepository.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("salePDS");
		u.setEmail("salePDS@example.com");
		u.setPassword("x");
		u.setRole(saleRole);
		u = userRepository.saveAndFlush(u);

		PropertySaleInfo info = new PropertySaleInfo();
		info.setUser(u);
		info.setFullName("Sale PDS");
		info.setPhone("0900");
		info = saleInfoRepository.saveAndFlush(info);

		Property p = new Property();
		p.setTitle("PD Service Prop");
		p.setPrice(new BigDecimal("123.45"));
		p.setDescription("d");
		p.setType(type);
		p.setSaleInfo(info);
		p.setArea(area);
		propertyId = propertyRepository.saveAndFlush(p).getPropertyId();

		PropertyDefineDetails d1 = new PropertyDefineDetails();
		d1.setDetailName("Size-S");
		d1.setIsNumber(true);
		d1.setUnit("m2");
		d1.setShowInHomePage(true);
		numericDetailId = defineRepository.saveAndFlush(d1).getDetailId();

		PropertyDefineDetails d2 = new PropertyDefineDetails();
		d2.setDetailName("Interior-S");
		d2.setIsNumber(false);
		d2.setUnit(null);
		d2.setShowInHomePage(false);
		textDetailId = defineRepository.saveAndFlush(d2).getDetailId();
	}

	@Test
	@Transactional
	@DisplayName("create/list/update/deleteOne/deleteAll flow works")
	void crud_flow() {
		PropertyDetailsCreateReq cReq = new PropertyDetailsCreateReq();
		PropertyDetailsItemReq i1 = new PropertyDetailsItemReq();
		i1.setDetailId(numericDetailId);
		i1.setValue("100");
		PropertyDetailsItemReq i2 = new PropertyDetailsItemReq();
		i2.setDetailId(textDetailId);
		i2.setValue("Full furniture");
		cReq.setItems(List.of(i1, i2));

		var created = service.create(propertyId, cReq);
		assertThat(created).hasSize(2);
		assertThat(created).extracting(r -> r.getDetailId()).contains(numericDetailId, textDetailId);

		var listed = service.listByProperty(propertyId);
		assertThat(listed).hasSize(2);

		PropertyDetailsUpdateReq uReq = new PropertyDetailsUpdateReq();
		PropertyDetailsItemReq u1 = new PropertyDetailsItemReq();
		u1.setDetailId(numericDetailId);
		u1.setValue("120");
		PropertyDetailsItemReq u2 = new PropertyDetailsItemReq();
		u2.setDetailId(textDetailId);
		u2.setValue("Basic furniture");
		uReq.setItems(List.of(u1, u2));

		var updated = service.update(propertyId, uReq);
		assertThat(updated).extracting(r -> r.getValue())
			.containsExactlyInAnyOrder("120", "Basic furniture");

		// deleteOne
		service.deleteOne(propertyId, numericDetailId);
		assertThat(detailsRepository.findById_DetailIdAndId_PropertyId(numericDetailId, propertyId)).isNotPresent();
		assertThat(detailsRepository.findById_DetailIdAndId_PropertyId(textDetailId, propertyId)).isPresent();

		// deleteAll
		service.deleteAll(propertyId);
		assertThat(detailsRepository.findByProperty_PropertyId(propertyId)).isEmpty();
	}

	@Test
	@Transactional
	@DisplayName("create with missing property or define ids throws ResourceNotFoundException")
	void create_not_found_cases() {
		// property not found
		PropertyDetailsCreateReq cReq = new PropertyDetailsCreateReq();
		PropertyDetailsItemReq i1 = new PropertyDetailsItemReq();
		i1.setDetailId(numericDetailId);
		i1.setValue("100");
		cReq.setItems(List.of(i1));
		assertThatThrownBy(() -> service.create(999999L, cReq))
			.isInstanceOf(ResourceNotFoundException.class);

		// define id missing
		PropertyDetailsCreateReq invalid = new PropertyDetailsCreateReq();
		PropertyDetailsItemReq ix = new PropertyDetailsItemReq();
		ix.setDetailId(999999);
		ix.setValue("x");
		invalid.setItems(List.of(ix));
		assertThatThrownBy(() -> service.create(propertyId, invalid))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@Transactional
	@DisplayName("create with duplicate details for same property throws ConflictException")
	void create_conflict_on_duplicates() {
		PropertyDetailsCreateReq cReq = new PropertyDetailsCreateReq();
		PropertyDetailsItemReq i1 = new PropertyDetailsItemReq();
		i1.setDetailId(numericDetailId);
		i1.setValue("100");
		cReq.setItems(List.of(i1));

		service.create(propertyId, cReq);
		assertThatThrownBy(() -> service.create(propertyId, cReq))
			.isInstanceOf(ConflictException.class);
	}

	@Test
	@Transactional
	@DisplayName("numeric details with non-numeric value in create/update throw BusinessException")
	void numeric_value_validation() {
		PropertyDetailsCreateReq badCreate = new PropertyDetailsCreateReq();
		PropertyDetailsItemReq i1 = new PropertyDetailsItemReq();
		i1.setDetailId(numericDetailId);
		i1.setValue("abc");
		badCreate.setItems(List.of(i1));
		assertThatThrownBy(() -> service.create(propertyId, badCreate))
			.isInstanceOf(BusinessException.class);

		// prepare valid first
		PropertyDetailsCreateReq cReq = new PropertyDetailsCreateReq();
		PropertyDetailsItemReq v1 = new PropertyDetailsItemReq();
		v1.setDetailId(numericDetailId);
		v1.setValue("100");
		cReq.setItems(List.of(v1));
		service.create(propertyId, cReq);

		PropertyDetailsUpdateReq badUpdate = new PropertyDetailsUpdateReq();
		PropertyDetailsItemReq u1 = new PropertyDetailsItemReq();
		u1.setDetailId(numericDetailId);
		u1.setValue("xyz");
		badUpdate.setItems(List.of(u1));
		assertThatThrownBy(() -> service.update(propertyId, badUpdate))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	@Transactional
	@DisplayName("update/list/deleteOne/deleteAll with non-existing property or details throws not found")
	void not_found_variants() {
		PropertyDetailsUpdateReq uReq = new PropertyDetailsUpdateReq();
		PropertyDetailsItemReq u1 = new PropertyDetailsItemReq();
		u1.setDetailId(numericDetailId);
		u1.setValue("100");
		uReq.setItems(List.of(u1));

		// property not found variants
		assertThatThrownBy(() -> service.update(999999L, uReq))
			.isInstanceOf(ResourceNotFoundException.class);
		assertThatThrownBy(() -> service.listByProperty(999999L))
			.isInstanceOf(ResourceNotFoundException.class);
		assertThatThrownBy(() -> service.deleteAll(999999L))
			.isInstanceOf(ResourceNotFoundException.class);

		// deleteOne / update non-existing detail on existing property
		assertThatThrownBy(() -> service.deleteOne(propertyId, 999999))
			.isInstanceOf(ResourceNotFoundException.class);

		PropertyDetailsUpdateReq badDetail = new PropertyDetailsUpdateReq();
		PropertyDetailsItemReq bx = new PropertyDetailsItemReq();
		bx.setDetailId(999999);
		bx.setValue("v");
		badDetail.setItems(List.of(bx));
		assertThatThrownBy(() -> service.update(propertyId, badDetail))
			.isInstanceOf(ResourceNotFoundException.class);
	}
}
