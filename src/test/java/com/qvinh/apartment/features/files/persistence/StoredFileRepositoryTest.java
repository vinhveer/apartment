package com.qvinh.apartment.features.files.persistence;

import com.qvinh.apartment.features.files.domain.StoredFileMeta;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class StoredFileRepositoryTest extends PostgresTestContainer {

	@Autowired private StoredFileMetaRepository fileRepo;

	@Test
	@DisplayName("unique constraint on sha256")
	void unique_sha256() {
		StoredFileMeta a = new StoredFileMeta();
		a.setOriginalName("a.jpg");
		a.setStoredName("a.jpg");
		a.setExt("jpg");
		a.setMimeType("image/jpeg");
		a.setSizeBytes(10);
		a.setSha256("0".repeat(64));
		a.setAccessLevel("PUBLIC");
		a.setLocation("LOCAL");
		a.setRelativePath("public/2025/01/a.jpg");
		fileRepo.saveAndFlush(a);

		StoredFileMeta b = new StoredFileMeta();
		b.setOriginalName("b.jpg");
		b.setStoredName("b.jpg");
		b.setExt("jpg");
		b.setMimeType("image/jpeg");
		b.setSizeBytes(11);
		b.setSha256("0".repeat(64));
		b.setAccessLevel("PUBLIC");
		b.setLocation("LOCAL");
		b.setRelativePath("public/2025/01/b.jpg");
		assertThatThrownBy(() -> fileRepo.saveAndFlush(b))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("findBySha256 returns saved entry")
	void find_by_sha256() {
		StoredFileMeta a = new StoredFileMeta();
		a.setOriginalName("a.jpg");
		a.setStoredName("a.jpg");
		a.setExt("jpg");
		a.setMimeType("image/jpeg");
		a.setSizeBytes(10);
		a.setSha256("1".repeat(64));
		a.setAccessLevel("PRIVATE");
		a.setLocation("LOCAL");
		a.setRelativePath("private/2025/01/a.jpg");
		fileRepo.saveAndFlush(a);
		assertThat(fileRepo.findBySha256(Objects.requireNonNull("1".repeat(64)))).isPresent();
		assertThat(fileRepo.findBySha256(Objects.requireNonNull("2".repeat(64)))).isNotPresent();
	}
}

