package com.qminh.apartment.repository;

import com.qminh.apartment.entity.PropertyDefineDetails;
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
class PropertyDefineDetailsRepositoryTest extends PostgresTestContainer {

	@Autowired
	private PropertyDefineDetailsRepository repository;

	@Test
	@DisplayName("save() persists and generates id")
	void save_ok() {
		PropertyDefineDetails e = new PropertyDefineDetails();
		e.setDetailName("bedrooms");
		PropertyDefineDetails saved = repository.saveAndFlush(e);
		assertThat(Objects.requireNonNull(saved.getDetailId())).isNotNull();
		assertThat(repository.findById(Objects.requireNonNull(saved.getDetailId()))).isPresent();
	}

	@Test
	@DisplayName("unique constraint on detail_name")
	void unique_detail_name() {
		PropertyDefineDetails d1 = new PropertyDefineDetails();
		d1.setDetailName("dup");
		repository.saveAndFlush(d1);

		PropertyDefineDetails d2 = new PropertyDefineDetails();
		d2.setDetailName("dup");

		assertThatThrownBy(() -> repository.saveAndFlush(d2))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("existsByDetailName returns true when name exists")
	void exists_by_name() {
		PropertyDefineDetails d1 = new PropertyDefineDetails();
		d1.setDetailName("n1");
		repository.saveAndFlush(d1);
		assertThat(repository.existsByDetailName("n1")).isTrue();
		assertThat(repository.existsByDetailName("n2")).isFalse();
	}
}


