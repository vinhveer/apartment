package com.qminh.apartment.service;

import com.qminh.apartment.dto.file.FileMetaUpdateReq;
import com.qminh.apartment.entity.StoredFileMeta;
import com.qminh.apartment.entity.StoredFileVariant;
import com.qminh.apartment.repository.StoredFileMetaRepository;
import com.qminh.apartment.repository.StoredFileVariantRepository;
import com.qminh.apartment.service.impl.FileService;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.util.Objects;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(properties = {
	"app.upload.base-path=./build/test-uploads"
})
class FileServiceTest extends PostgresTestContainer {

	@Autowired private FileService fileService;
	@Autowired private StoredFileVariantRepository variantRepo;
	@Autowired private StoredFileMetaRepository fileRepo;

	private static MultipartFile sampleJpeg(String name) throws Exception {
		BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(img, "jpg", baos);
			return new MockMultipartFile("file", name, "image/jpeg", baos.toByteArray());
		}
	}

	@Test
	@Transactional
	@DisplayName("upload image PUBLIC: saves original and generates variants; de-dup works")
	void upload_public_image_generates_variants_and_dedup() throws Exception {
		MultipartFile mf = sampleJpeg("x.jpg");
		StoredFileMeta meta1 = fileService.upload(mf, "PUBLIC");
		assertThat(meta1.getFileId()).isNotNull();
		assertThat(meta1.getAccessLevel()).isEqualTo("PUBLIC");
		assertThat(Files.exists(Path.of("./build/test-uploads").resolve(meta1.getRelativePath()))).isTrue();
		var variants = variantRepo.findByFile_FileId(Objects.requireNonNull(meta1.getFileId()));
		assertThat(variants.size()).isGreaterThanOrEqualTo(1);

		// de-dup
		StoredFileMeta meta2 = fileService.upload(sampleJpeg("anyname.jpg"), "PUBLIC");
		assertThat(meta2.getFileId()).isEqualTo(meta1.getFileId());
	}

	@Test
	@Transactional
	@DisplayName("rename updates originalName but keeps storedName and relativePath")
	void rename_updates_original_name_only() throws Exception {
		MultipartFile mf = sampleJpeg("orig.jpg");
		StoredFileMeta meta = fileService.upload(mf, "PUBLIC");
		Long id = java.util.Objects.requireNonNull(meta.getFileId());
		String oldStoredName = meta.getStoredName();
		String oldPath = meta.getRelativePath();

		StoredFileMeta renamed = fileService.rename(id, "renamed-image.jpg");
		assertThat(renamed.getOriginalName()).isEqualTo("renamed-image.jpg");
		assertThat(renamed.getStoredName()).isEqualTo(oldStoredName);
		assertThat(renamed.getRelativePath()).isEqualTo(oldPath);
	}

	@Test
	@Transactional
	@DisplayName("delete removes meta, variants and physical files")
	void delete_removes_all() throws Exception {
		MultipartFile mf = sampleJpeg("del.jpg");
		StoredFileMeta meta = fileService.upload(mf, "PUBLIC");
		Long id = java.util.Objects.requireNonNull(meta.getFileId());
		var variants = variantRepo.findByFile_FileId(id);

		Path base = Path.of("./build/test-uploads");
		assertThat(Files.exists(base.resolve(meta.getRelativePath()))).isTrue();
		for (StoredFileVariant v : variants) {
			assertThat(Files.exists(base.resolve(v.getRelativePath()))).isTrue();
		}

		fileService.delete(id);
		assertThat(fileRepo.findById(id)).isNotPresent();
		assertThat(variantRepo.findByFile_FileId(id)).isEmpty();
		assertThat(Files.exists(base.resolve(meta.getRelativePath()))).isFalse();
		for (StoredFileVariant v : variants) {
			assertThat(Files.exists(base.resolve(v.getRelativePath()))).isFalse();
		}
	}

	@Test
	@Transactional
	@DisplayName("search filters by accessLevel, mimeType and search on name/title/tags")
	void search_filters_by_access_mime_and_search() throws Exception {
		// create PUBLIC image with banner keyword
		StoredFileMeta banner = fileService.upload(sampleJpeg("banner-homepage.jpg"), "PUBLIC");
		Long bannerId = java.util.Objects.requireNonNull(banner.getFileId());
		FileMetaUpdateReq meta1 = new FileMetaUpdateReq();
		meta1.setTitle("Homepage main banner");
		meta1.setTags(java.util.List.of("banner", "homepage", "tet-2025"));
		fileService.updateMeta(bannerId, meta1);

		// create another PUBLIC image without banner keyword
		fileService.upload(sampleJpeg("hero.jpg"), "PUBLIC");

		// create PRIVATE image with banner keyword (should be filtered out when accessLevel=PUBLIC)
		fileService.upload(sampleJpeg("banner-private.jpg"), "PRIVATE");

		// search PUBLIC images with keyword 'banner'
		var page = fileService.search(
			"PUBLIC",
			"image",
			"banner",
			null,
			null,
			PageRequest.of(0, 10)
		);
		assertThat(page.getContent())
			.extracting(StoredFileMeta::getFileId)
			.contains(bannerId);
		assertThat(page.getContent())
			.allMatch(m -> "PUBLIC".equals(m.getAccessLevel()));

		// createdFrom in future yields empty result
		java.time.LocalDate tomorrow = java.time.LocalDate.now().plusDays(1);
		var emptyPage = fileService.search(
			null,
			null,
			null,
			tomorrow,
			null,
			PageRequest.of(0, 10)
		);
		assertThat(emptyPage.getContent()).isEmpty();
	}

	@Test
	@Transactional
	@DisplayName("updateMeta updates alt/title/description/tags and can clear tags")
	void update_meta_full_and_clear_tags() throws Exception {
		StoredFileMeta meta = fileService.upload(sampleJpeg("meta.jpg"), "PUBLIC");
		Long id = java.util.Objects.requireNonNull(meta.getFileId());

		// full update
		FileMetaUpdateReq full = new FileMetaUpdateReq();
		full.setAltText("Alt text");
		full.setTitle("Title text");
		full.setDescription("Description text");
		full.setTags(java.util.List.of("a", "b"));
		StoredFileMeta updated = fileService.updateMeta(id, full);
		assertThat(updated.getAltText()).isEqualTo("Alt text");
		assertThat(updated.getTitle()).isEqualTo("Title text");
		assertThat(updated.getDescription()).isEqualTo("Description text");
		assertThat(updated.getTags()).isEqualTo("a,b");

		// clear tags
		FileMetaUpdateReq clearTags = new FileMetaUpdateReq();
		clearTags.setTags(java.util.List.of());
		StoredFileMeta cleared = fileService.updateMeta(id, clearTags);
		assertThat(cleared.getTags()).isNull();
	}
}


