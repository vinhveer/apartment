package com.qminh.apartment.service;

import com.qminh.apartment.dto.property.PropertyCreateReq;
import com.qminh.apartment.dto.property.PropertyUpdateReq;
import com.qminh.apartment.entity.*;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.repository.*;
import com.qminh.apartment.service.impl.PropertyService;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PropertyServiceTest extends PostgresTestContainer {

	@Autowired private PropertyService service;
	@Autowired private PropertyTypeRepository typeRepository;
	@Autowired private PropertyAreaRepository areaRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private PropertySaleInfoRepository saleInfoRepository;

	private Integer typeId;
	private Integer areaId;
	private Long saleUserId;

	@BeforeEach
	void setup() {
		PropertyType type = new PropertyType(); type.setTypeName("TypeS");
		typeId = typeRepository.saveAndFlush(type).getTypeId();
		PropertyArea area = new PropertyArea(); area.setAreaName("AreaS"); area.setAreaLink("area-s");
		areaId = areaRepository.saveAndFlush(area).getAreaId();
		Role saleRole = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("SALE"); return roleRepository.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("saleS");
		u.setEmail("saleS@example.com");
		u.setPassword("x");
		u.setRole(saleRole);
		saleUserId = userRepository.saveAndFlush(u).getId();
		PropertySaleInfo info = new PropertySaleInfo();
		info.setUser(u);
		info.setFullName("Sale S");
		info.setPhone("0900");
		saleInfoRepository.saveAndFlush(info);
	}

	@Test
	@Transactional
	@DisplayName("Property create/get/update/delete works")
	void crud_flow() {
		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("T1");
		req.setPrice(new BigDecimal("100.00"));
		req.setDescription("d");
		req.setTypeId(typeId);
		req.setSaleUserId(saleUserId);
		req.setAreaId(areaId);
		req.setIsPublic(true);
		var created = service.create(req);
		assertThat(created.getPropertyId()).isNotNull();

		var got = service.get(created.getPropertyId());
		assertThat(got.getTitle()).isEqualTo("T1");

		PropertyUpdateReq up = new PropertyUpdateReq();
		up.setTitle("T2");
		up.setPrice(new BigDecimal("200.00"));
		up.setDescription("d2");
		up.setTypeId(typeId);
		up.setSaleUserId(saleUserId);
		up.setAreaId(areaId);
		up.setIsPublic(false);
		var updated = service.update(created.getPropertyId(), up);
		assertThat(updated.getTitle()).isEqualTo("T2");
	}

	@Test
	@Transactional
	@DisplayName("delete then get throws not found")
	void delete_then_notfound() {
		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("TD");
		req.setPrice(new BigDecimal("150.00"));
		req.setDescription("d");
		req.setTypeId(typeId);
		req.setSaleUserId(saleUserId);
		req.setAreaId(areaId);
		req.setIsPublic(false);
		var created = service.create(req);
		Long deletedId = created.getPropertyId();
		service.delete(deletedId);
		assertThatThrownBy(() -> service.get(deletedId))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@Transactional
	@DisplayName("list(Pageable) supports pagination and sort; null pageable throws NPE")
	void list_pageable_and_null() {
		for (int i = 0; i < 5; i++) {
			PropertyCreateReq req = new PropertyCreateReq();
			req.setTitle("Tx_" + System.nanoTime() + "_" + i);
			req.setPrice(new BigDecimal("100.00"));
			req.setDescription("d");
			req.setTypeId(typeId);
			req.setSaleUserId(saleUserId);
			req.setAreaId(areaId);
			req.setIsPublic(true);
			service.create(req);
		}
		Pageable p = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "propertyId"));
		var page = service.list(p);
		assertThat(page.getContent()).hasSizeLessThanOrEqualTo(3);
		assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(5);
		assertThatThrownBy(() -> service.list(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@Transactional
	@DisplayName("not found cases: get/update/delete with non-existing id")
	void not_found_cases() {
		assertThatThrownBy(() -> service.get(999999)).isInstanceOf(ResourceNotFoundException.class);
		PropertyUpdateReq up = new PropertyUpdateReq();
		up.setTitle("N");
		up.setPrice(new BigDecimal("1.00"));
		up.setDescription("n");
		up.setTypeId(typeId);
		up.setSaleUserId(saleUserId);
		up.setAreaId(areaId);
		up.setIsPublic(true);
		assertThatThrownBy(() -> service.update(999999, up)).isInstanceOf(ResourceNotFoundException.class);
		assertThatThrownBy(() -> service.delete(999999)).isInstanceOf(ResourceNotFoundException.class);
	}
}


