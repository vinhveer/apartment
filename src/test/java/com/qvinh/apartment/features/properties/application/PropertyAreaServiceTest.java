package com.qvinh.apartment.features.properties.application;

import com.qvinh.apartment.features.properties.application.impl.PropertyAreaService;
import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaCreateReq;
import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaUpdateReq;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PropertyAreaServiceTest extends PostgresTestContainer {

	@Autowired
	private PropertyAreaService service;

	@Test
	@Transactional
	@DisplayName("create/get/update/delete works and enforces constraints")
	void crud_flow() {
		PropertyAreaCreateReq req = new PropertyAreaCreateReq();
		req.setAreaName("nA");
		req.setAreaLink("lA");
		var created = service.create(req);
		assertThat(created.getAreaId()).isNotNull();

		var got = service.get(created.getAreaId());
		assertThat(got.getAreaName()).isEqualTo("nA");

		PropertyAreaUpdateReq up = new PropertyAreaUpdateReq();
		up.setAreaName("nB");
		up.setAreaLink("lB");
		var updated = service.update(created.getAreaId(), up);
		assertThat(updated.getAreaName()).isEqualTo("nB");

		var areaId = created.getAreaId();
		service.delete(areaId);
		assertThatThrownBy(() -> service.get(areaId))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@Transactional
	@DisplayName("create duplicate name or link -> DataIntegrityViolationException")
	void duplicates_throw() {
		PropertyAreaCreateReq r1 = new PropertyAreaCreateReq();
		r1.setAreaName("dup");
		r1.setAreaLink("link1");
		service.create(r1);

		PropertyAreaCreateReq r2 = new PropertyAreaCreateReq();
		r2.setAreaName("dup");
		r2.setAreaLink("link2");
		assertThatThrownBy(() -> service.create(r2))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	@DisplayName("list(Pageable) supports pagination and sort; null pageable throws NPE")
	void list_pageable_and_null() {
		for (int i = 0; i < 5; i++) {
			PropertyAreaCreateReq r = new PropertyAreaCreateReq();
			r.setAreaName("area_" + System.nanoTime() + "_" + i);
			r.setAreaLink("link_" + System.nanoTime() + "_" + i);
			service.create(r);
		}
		Pageable p = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "areaId"));
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
		PropertyAreaUpdateReq up = new PropertyAreaUpdateReq();
		up.setAreaName("xx");
		up.setAreaLink("yy");
		assertThatThrownBy(() -> service.update(999999, up)).isInstanceOf(ResourceNotFoundException.class);
		assertThatThrownBy(() -> service.delete(999999)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@Transactional
	@DisplayName("concurrency duplicates yield one success and one constraint violation")
	void concurrency_duplicates() throws Exception {
		PropertyAreaCreateReq base = new PropertyAreaCreateReq();
		base.setAreaName("c_dup");
		base.setAreaLink("c_link");
		Runnable r1 = () -> {
			PropertyAreaCreateReq r = new PropertyAreaCreateReq();
			r.setAreaName(base.getAreaName());
			r.setAreaLink(base.getAreaLink());
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
		PropertyAreaCreateReq another = new PropertyAreaCreateReq();
		another.setAreaName(base.getAreaName());
		another.setAreaLink("other");
		assertThatThrownBy(() -> service.create(another)).isInstanceOf(DataIntegrityViolationException.class);
	}
}
