package com.qminh.apartment.service;

import com.qminh.apartment.dto.area.PropertyAreaCreateReq;
import com.qminh.apartment.dto.area.PropertyAreaUpdateReq;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.service.impl.PropertyAreaService;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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
}


