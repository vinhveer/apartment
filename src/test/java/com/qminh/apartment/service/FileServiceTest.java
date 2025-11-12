package com.qminh.apartment.service;

import com.qminh.apartment.entity.StoredFileMeta;
import com.qminh.apartment.repository.StoredFileVariantRepository;
import com.qminh.apartment.service.impl.FileService;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
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
}


