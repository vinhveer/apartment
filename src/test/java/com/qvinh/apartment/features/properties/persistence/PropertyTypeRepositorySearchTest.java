package com.qvinh.apartment.features.properties.persistence;

import com.qvinh.apartment.features.properties.domain.PropertyType;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PropertyTypeRepositorySearchTest extends PostgresTestContainer {

	@Autowired
	private PropertyTypeRepository repository;

	@Test
	@DisplayName("findByTypeNameContainingIgnoreCase honors keyword and pagination")
	void find_by_type_name_containing_ignore_case() {
		for (int i = 0; i < 3; i++) {
			PropertyType type = new PropertyType();
			type.setTypeName("Luxury " + i);
			repository.saveAndFlush(type);
		}
		PropertyType other = new PropertyType();
		other.setTypeName("Standard");
		repository.saveAndFlush(other);

		var page = repository.findByTypeNameContainingIgnoreCase("lux", PageRequest.of(0, 2));
		assertThat(page.getTotalElements()).isEqualTo(3);
		assertThat(page.getContent()).hasSize(2);
		assertThat(page.getContent()).allMatch(pt ->
			Objects.requireNonNull(pt.getTypeName()).toLowerCase().contains("lux"));
	}
}
