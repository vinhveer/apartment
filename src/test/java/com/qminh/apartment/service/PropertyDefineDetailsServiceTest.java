package com.qminh.apartment.service;

import com.qminh.apartment.dto.property_define_details.PropertyDefineDetailsCreateReq;
import com.qminh.apartment.dto.property_define_details.PropertyDefineDetailsUpdateReq;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.service.impl.PropertyDefineDetailsService;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PropertyDefineDetailsServiceTest extends PostgresTestContainer {

	@Autowired
	private PropertyDefineDetailsService service;

	@Test
	@Transactional
	@DisplayName("create/get/update/delete works and enforces constraints")
	void crud_flow() {
		PropertyDefineDetailsCreateReq req = new PropertyDefineDetailsCreateReq();
		req.setDetailName("bedrooms");
		req.setIsNumber(true);
		req.setUnit("room");
		req.setShowInHomePage(true);
		var created = service.create(req);
		assertThat(created.getDetailId()).isNotNull();

		var got = service.get(created.getDetailId());
		assertThat(got.getDetailName()).isEqualTo("bedrooms");
		assertThat(got.getIsNumber()).isTrue();
		assertThat(got.getUnit()).isEqualTo("room");

		PropertyDefineDetailsUpdateReq up = new PropertyDefineDetailsUpdateReq();
		up.setDetailName("bathrooms");
		up.setIsNumber(true);
		up.setUnit("room");
		up.setShowInHomePage(false);
		var updated = service.update(created.getDetailId(), up);
		assertThat(updated.getDetailName()).isEqualTo("bathrooms");
		assertThat(updated.getShowInHomePage()).isFalse();

		var id = created.getDetailId();
		service.delete(id);
		assertThatThrownBy(() -> service.get(id))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@Transactional
	@DisplayName("create duplicate name -> DataIntegrityViolationException")
	void duplicates_throw() {
		PropertyDefineDetailsCreateReq r1 = new PropertyDefineDetailsCreateReq();
		r1.setDetailName("dup");
		r1.setIsNumber(false);
		r1.setShowInHomePage(false);
		service.create(r1);

		PropertyDefineDetailsCreateReq r2 = new PropertyDefineDetailsCreateReq();
		r2.setDetailName("dup");
		r2.setIsNumber(false);
		r2.setShowInHomePage(false);
		assertThatThrownBy(() -> service.create(r2))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	@DisplayName("list(Pageable) supports pagination and sort; null pageable throws NPE")
	void list_pageable_and_null() {
		for (int i = 0; i < 5; i++) {
			PropertyDefineDetailsCreateReq r = new PropertyDefineDetailsCreateReq();
			r.setDetailName("d_" + System.nanoTime() + "_" + i);
			r.setIsNumber(false);
			r.setShowInHomePage(false);
			service.create(r);
		}
		Pageable p = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "detailId"));
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
		PropertyDefineDetailsUpdateReq up = new PropertyDefineDetailsUpdateReq();
		up.setDetailName("x");
		up.setIsNumber(false);
		up.setShowInHomePage(false);
		assertThatThrownBy(() -> service.update(999999, up)).isInstanceOf(ResourceNotFoundException.class);
		assertThatThrownBy(() -> service.delete(999999)).isInstanceOf(ResourceNotFoundException.class);
	}
}


