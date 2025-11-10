package com.qminh.apartment.repository;

import com.qminh.apartment.entity.PropertyType;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PropertyTypeRepositoryTest extends PostgresTestContainer {

	@Autowired
	private PropertyTypeRepository repository;

	@Test
	@DisplayName("save() persists and generates id")
	void save_ok() {
		PropertyType t = new PropertyType();
		t.setTypeName("Apartment");
		PropertyType saved = repository.saveAndFlush(t);
		assertThat(Objects.requireNonNull(saved.getTypeId())).isNotNull();
		assertThat(repository.findById(Objects.requireNonNull(saved.getTypeId()))).isPresent();
	}

	@Test
	@DisplayName("unique constraint on type_name")
	void unique_type_name() {
		PropertyType t1 = new PropertyType();
		t1.setTypeName("DupType");
		repository.saveAndFlush(t1);

		PropertyType t2 = new PropertyType();
		t2.setTypeName("DupType");

		assertThatThrownBy(() -> repository.saveAndFlush(t2))
			.isInstanceOf(DataIntegrityViolationException.class);
	}
}