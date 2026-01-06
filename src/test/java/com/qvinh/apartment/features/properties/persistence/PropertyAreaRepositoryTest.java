package com.qvinh.apartment.features.properties.persistence;

import com.qvinh.apartment.features.properties.domain.PropertyArea;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.Objects;

@DataJpaTest
class PropertyAreaRepositoryTest extends PostgresTestContainer {

	@Autowired
	private PropertyAreaRepository repository;

	@Test
	@DisplayName("save() persists and generates id")
	void save_ok() {
		PropertyArea a = new PropertyArea();
		a.setAreaName("District 1");
		a.setAreaLink("district-1");
		PropertyArea saved = repository.saveAndFlush(a);
		assertThat(Objects.requireNonNull(saved.getAreaId())).isNotNull();
		assertThat(repository.findById(Objects.requireNonNull(saved.getAreaId()))).isPresent();
	}

	@Test
	@DisplayName("unique constraint on area_name")
	void unique_area_name() {
		PropertyArea a1 = new PropertyArea();
		a1.setAreaName("DupName");
		a1.setAreaLink("l1");
		repository.saveAndFlush(a1);

		PropertyArea a2 = new PropertyArea();
		a2.setAreaName("DupName");
		a2.setAreaLink("l2");

		assertThatThrownBy(() -> repository.saveAndFlush(a2))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("unique constraint on area_link")
	void unique_area_link() {
		PropertyArea a1 = new PropertyArea();
		a1.setAreaName("n1");
		a1.setAreaLink("dup-link");
		repository.saveAndFlush(a1);

		PropertyArea a2 = new PropertyArea();
		a2.setAreaName("n2");
		a2.setAreaLink("dup-link");

		assertThatThrownBy(() -> repository.saveAndFlush(a2))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("existsByAreaName/existsByAreaLink true/false and edge cases")
	void exists_checks() {
		PropertyArea a = new PropertyArea();
		a.setAreaName("District X");
		a.setAreaLink("district-x");
		repository.saveAndFlush(a);
		assertThat(repository.existsByAreaName("District X")).isTrue();
		assertThat(repository.existsByAreaName("district x")).isFalse();
		assertThat(repository.existsByAreaName(" District X ")).isFalse();
		assertThat(repository.existsByAreaLink("district-x")).isTrue();
		assertThat(repository.existsByAreaLink("DISTRICT-X")).isFalse();
		assertThat(repository.existsByAreaLink(" district-x ")).isFalse();
		assertThat(repository.existsByAreaName("Unknown")).isFalse();
		assertThat(repository.existsByAreaLink("unknown")).isFalse();
	}

	@Test
	@DisplayName("findByAreaLink present/empty and case sensitivity")
	void find_by_area_link() {
		String link = "area-l-" + System.nanoTime();
		newArea("Area L", link);
		assertThat(repository.findByAreaLink(link)).isPresent();
		assertThat(repository.findByAreaLink(Objects.requireNonNull(link.toUpperCase()))).isNotPresent();
		assertThat(repository.findByAreaLink(" " + link + " ")).isNotPresent();
		assertThat(repository.findByAreaLink("not-exist")).isNotPresent();
	}

	private @NonNull PropertyArea newArea(String name, String link) {
		PropertyArea a = new PropertyArea();
		a.setAreaName(name);
		a.setAreaLink(link);
		return repository.saveAndFlush(a);
	}
}

