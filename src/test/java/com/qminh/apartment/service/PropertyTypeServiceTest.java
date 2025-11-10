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
}


