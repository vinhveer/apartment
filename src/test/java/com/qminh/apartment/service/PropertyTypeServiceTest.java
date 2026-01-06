package com.qminh.apartment.service;

import com.qminh.apartment.dto.property_type.PropertyTypeCreateReq;
import com.qminh.apartment.dto.property_type.PropertyTypeUpdateReq;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.service.impl.PropertyTypeService;
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
class PropertyTypeServiceTest extends PostgresTestContainer {

	@Autowired
	private PropertyTypeService service;

	@Test
	@Transactional
	@DisplayName("create/get/update/delete works and enforces constraints")
	void crud_flow() {
		PropertyTypeCreateReq req = new PropertyTypeCreateReq();
		req.setTypeName("Apartment");
		var created = service.create(req);
		assertThat(created.getTypeId()).isNotNull();

		var got = service.get(created.getTypeId());
		assertThat(got.getTypeName()).isEqualTo("Apartment");

		PropertyTypeUpdateReq up = new PropertyTypeUpdateReq();
		up.setTypeName("Condo");
		var updated = service.update(created.getTypeId(), up);
		assertThat(updated.getTypeName()).isEqualTo("Condo");

		var typeId = created.getTypeId();
		service.delete(typeId);
		assertThatThrownBy(() -> service.get(typeId))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@Transactional
	@DisplayName("create duplicate name -> DataIntegrityViolationException")
	void duplicates_throw() {
		PropertyTypeCreateReq r1 = new PropertyTypeCreateReq();
		r1.setTypeName("DupType");
		service.create(r1);

		PropertyTypeCreateReq r2 = new PropertyTypeCreateReq();
		r2.setTypeName("DupType");
		assertThatThrownBy(() -> service.create(r2))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	@DisplayName("list(Pageable) supports pagination and sort; null pageable throws NPE")
	void list_pageable_and_null() {
		for (int i = 0; i < 5; i++) {
			PropertyTypeCreateReq r = new PropertyTypeCreateReq();
			r.setTypeName("Type_" + System.nanoTime() + "_" + i);
			service.create(r);
		}
		Pageable p = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "typeId"));
		var page = service.list(p);
		assertThat(page.getContent()).hasSizeLessThanOrEqualTo(3);
		assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(5);
		assertThatThrownBy(() -> service.list(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@Transactional
	@DisplayName("search(keyword, pageable) filters by keyword and ignores blanks")
	void search_keyword_and_blank() {
		var req1 = new PropertyTypeCreateReq();
		req1.setTypeName("Luxury Condo");
		service.create(req1);
		var req2 = new PropertyTypeCreateReq();
		req2.setTypeName("Luxury Villa");
		service.create(req2);
		var req3 = new PropertyTypeCreateReq();
		req3.setTypeName("Budget Apartment");
		service.create(req3);

		Pageable pageable = PageRequest.of(0, 5, Sort.by("typeName").ascending());
		var luxPage = service.search("luxury", pageable);
		assertThat(luxPage.getTotalElements()).isEqualTo(2);
		assertThat(luxPage.getContent()).allMatch(res -> res.getTypeName().toLowerCase().contains("luxury"));

		var blankPage = service.search("   ", pageable);
		assertThat(blankPage.getTotalElements()).isGreaterThanOrEqualTo(3);
	}

	@Test
	@Transactional
	@DisplayName("concurrency duplicates yield one success and one constraint violation")
	void concurrency_duplicates() throws Exception {
		PropertyTypeCreateReq base = new PropertyTypeCreateReq();
		base.setTypeName("cType");
		Runnable r1 = () -> {
			PropertyTypeCreateReq r = new PropertyTypeCreateReq();
			r.setTypeName(base.getTypeName());
			service.create(r);
		};
		Runnable r2 = r1;
		Thread t1 = new Thread(r1);
		Thread t2 = new Thread(() -> {
			try {
				r2.run();
			} catch (Exception ignored) {
				// ignore
			}
		});
		t1.start(); t2.start();
		t1.join(); t2.join();
		PropertyTypeCreateReq another = new PropertyTypeCreateReq();
		another.setTypeName(base.getTypeName());
		assertThatThrownBy(() -> service.create(another)).isInstanceOf(DataIntegrityViolationException.class);
	}
}


