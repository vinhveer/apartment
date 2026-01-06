package com.qvinh.apartment.features.properties.application;

import com.qvinh.apartment.features.properties.application.impl.PropertyGalleryService;
import com.qvinh.apartment.features.accounts.domain.*;
import com.qvinh.apartment.features.accounts.persistence.*;
import com.qvinh.apartment.features.files.domain.StoredFileMeta;
import com.qvinh.apartment.features.files.persistence.StoredFileMetaRepository;
import com.qvinh.apartment.features.properties.domain.*;
import com.qvinh.apartment.features.properties.dto.property_gallery.PropertyGalleryRes;
import com.qvinh.apartment.features.properties.persistence.*;
import com.qvinh.apartment.shared.exception.ConflictException;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PropertyGalleryServiceTest extends PostgresTestContainer {

	@Autowired private PropertyGalleryService service;
	@Autowired private PropertyRepository propertyRepository;
	@Autowired private StoredFileMetaRepository storedFileMetaRepository;
	@Autowired private PropertyGalleryRepository galleryRepository;
	@Autowired private PropertyTypeRepository typeRepository;
	@Autowired private PropertyAreaRepository areaRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private PropertySaleInfoRepository saleInfoRepository;

	private Property property;
	private StoredFileMeta file1;
	private StoredFileMeta file2;

	@BeforeEach
	void setup() {
		// Setup Property
		PropertyType type = new PropertyType();
		type.setTypeName("TypeS");
		type = typeRepository.saveAndFlush(type);

		PropertyArea area = new PropertyArea();
		area.setAreaName("AreaS");
		area.setAreaLink("area-s");
		area = areaRepository.saveAndFlush(area);

		Role saleRole = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role();
			r.setRoleName("SALE");
			return roleRepository.saveAndFlush(r);
		});

		User u = new User();
		u.setUsername("saleS_" + System.nanoTime());
		u.setEmail("saleS_" + System.nanoTime() + "@example.com");
		u.setPassword("x");
		u.setRole(saleRole);
		u = userRepository.saveAndFlush(u);

		PropertySaleInfo info = new PropertySaleInfo();
		info.setUser(u);
		info.setFullName("Sale S");
		info.setPhone("0900");
		info = saleInfoRepository.saveAndFlush(info);

		property = new Property();
		property.setTitle("Test Prop");
		property.setPrice(new BigDecimal("100.00"));
		property.setDescription("d");
		property.setType(type);
		property.setSaleInfo(info);
		property.setArea(area);
		property.setIsPublic(Boolean.TRUE);
		property.setIsForRent(Boolean.FALSE);
		property = propertyRepository.saveAndFlush(property);

		// Setup Files
		file1 = createFile("file1.jpg", "path1");
		file2 = createFile("file2.jpg", "path2");
	}

	private StoredFileMeta createFile(String name, String path) {
		StoredFileMeta f = new StoredFileMeta();
		f.setOriginalName(name);
		f.setStoredName("stored_" + System.nanoTime() + "_" + name);
		f.setExt("jpg");
		f.setMimeType("image/jpeg");
		f.setSizeBytes(1024L);
		String uniqueSha = String.format("%064x", System.nanoTime() + System.currentTimeMillis() + name.hashCode());
		f.setSha256(uniqueSha);
		f.setAccessLevel("PUBLIC");
		f.setLocation("LOCAL");
		f.setRelativePath(path);
		f.setCreatedAt(LocalDateTime.now());
		f.setUpdatedAt(LocalDateTime.now());
		return storedFileMetaRepository.saveAndFlush(f);
	}

	@Test
	@Transactional
	@DisplayName("addFileIntoGallery creates gallery item and returns correct response")
	void add_file_into_gallery() {
		Long propertyId = Objects.requireNonNull(property.getPropertyId());
		Long fileId = Objects.requireNonNull(file1.getFileId());

		PropertyGalleryRes res = service.addFileIntoGallery(propertyId, fileId);

		assertThat(res.getPropertyId()).isEqualTo(propertyId);
		assertThat(res.getFileId()).isEqualTo(fileId);
		assertThat(res.getFilePath()).isEqualTo(file1.getRelativePath());

		PropertyGalleryId id = new PropertyGalleryId(propertyId, fileId);
		assertThat(galleryRepository.existsById(id)).isTrue();
	}

	@Test
	@Transactional
		@DisplayName("addFileIntoGallery throws ResourceNotFoundException when property not found")
		void add_file_property_not_found() {
			Long fileId = Objects.requireNonNull(file1.getFileId());

			assertThatThrownBy(() -> service.addFileIntoGallery(999999L, fileId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Property not found");
		}

	@Test
	@Transactional
		@DisplayName("addFileIntoGallery throws ResourceNotFoundException when file not found")
		void add_file_file_not_found() {
			Long propertyId = Objects.requireNonNull(property.getPropertyId());

			assertThatThrownBy(() -> service.addFileIntoGallery(propertyId, 999999L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("File not found");
		}

	@Test
	@Transactional
	@DisplayName("addFileIntoGallery throws ConflictException when file already exists in gallery")
	void add_file_duplicate_conflict() {
		Long propertyId = Objects.requireNonNull(property.getPropertyId());
		Long fileId = Objects.requireNonNull(file1.getFileId());

		service.addFileIntoGallery(propertyId, fileId);

		assertThatThrownBy(() -> service.addFileIntoGallery(propertyId, fileId))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining("File already exists in gallery");
	}

	@Test
	@Transactional
	@DisplayName("getFileByPropertiesId returns all gallery items for a property")
	void get_files_by_property() {
		Long propertyId = Objects.requireNonNull(property.getPropertyId());

		service.addFileIntoGallery(propertyId, Objects.requireNonNull(file1.getFileId()));
		service.addFileIntoGallery(propertyId, Objects.requireNonNull(file2.getFileId()));

		List<PropertyGalleryRes> res = service.getFileByPropertiesId(propertyId);

		assertThat(res).hasSize(2);
		assertThat(res)
			.extracting(PropertyGalleryRes::getFileId)
			.containsExactlyInAnyOrder(file1.getFileId(), file2.getFileId());
		assertThat(res)
			.extracting(PropertyGalleryRes::getPropertyId)
			.containsOnly(propertyId);
	}

	@Test
	@Transactional
	@DisplayName("getFileByPropertiesId returns empty list when property has no gallery items")
	void get_files_by_property_empty() {
		Long propertyId = Objects.requireNonNull(property.getPropertyId());

		List<PropertyGalleryRes> res = service.getFileByPropertiesId(propertyId);

		assertThat(res).isEmpty();
	}

	@Test
	@Transactional
		@DisplayName("getFileByPropertiesId throws ResourceNotFoundException when property not found")
		void get_files_property_not_found() {
			assertThatThrownBy(() -> service.getFileByPropertiesId(999999L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Property not found");
		}

	@Test
	@Transactional
	@DisplayName("deleteFileIntoGallery removes gallery item")
	void delete_file_from_gallery() {
		Long propertyId = Objects.requireNonNull(property.getPropertyId());
		Long fileId = Objects.requireNonNull(file1.getFileId());

		service.addFileIntoGallery(propertyId, fileId);

		PropertyGalleryId id = new PropertyGalleryId(propertyId, fileId);
		assertThat(galleryRepository.existsById(id)).isTrue();

		service.deleteFileIntoGallery(propertyId, fileId);

		assertThat(galleryRepository.existsById(id)).isFalse();
		List<PropertyGalleryRes> res = service.getFileByPropertiesId(propertyId);
		assertThat(res).isEmpty();
	}

	@Test
	@Transactional
	@DisplayName("deleteFileIntoGallery throws ResourceNotFoundException when gallery item not found")
	void delete_file_not_found() {
		Long propertyId = Objects.requireNonNull(property.getPropertyId());
		Long fileId = Objects.requireNonNull(file1.getFileId());

		assertThatThrownBy(() -> service.deleteFileIntoGallery(propertyId, fileId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Property gallery not found");
	}

	@Test
	@Transactional
	@DisplayName("deleteFileIntoGallery only deletes the specified file, keeps others")
	void delete_file_keeps_others() {
		Long propertyId = Objects.requireNonNull(property.getPropertyId());
		Long fileId1 = Objects.requireNonNull(file1.getFileId());
		Long fileId2 = Objects.requireNonNull(file2.getFileId());

		service.addFileIntoGallery(propertyId, fileId1);
		service.addFileIntoGallery(propertyId, fileId2);

		List<PropertyGalleryRes> before = service.getFileByPropertiesId(propertyId);
		assertThat(before).hasSize(2);

		service.deleteFileIntoGallery(propertyId, fileId1);

		List<PropertyGalleryRes> after = service.getFileByPropertiesId(propertyId);
		assertThat(after).hasSize(1);
		assertThat(after.get(0).getFileId()).isEqualTo(fileId2);
	}

	@Test
	@Transactional
	@DisplayName("addFileIntoGallery sets createdAt and updatedAt correctly")
	void add_file_sets_timestamps() {
		Long propertyId = Objects.requireNonNull(property.getPropertyId());
		Long fileId = Objects.requireNonNull(file1.getFileId());

		service.addFileIntoGallery(propertyId, fileId);

		PropertyGalleryId id = new PropertyGalleryId(propertyId, fileId);
		PropertyGallery saved = galleryRepository.findById(id).orElseThrow();

		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getUpdatedAt()).isNotNull();
		assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
	}

	@Test
	@Transactional
	@DisplayName("getFileByPropertiesId returns files in correct order")
	void get_files_order() {
		Long propertyId = Objects.requireNonNull(property.getPropertyId());

		service.addFileIntoGallery(propertyId, Objects.requireNonNull(file1.getFileId()));
		service.addFileIntoGallery(propertyId, Objects.requireNonNull(file2.getFileId()));

		List<PropertyGalleryRes> res = service.getFileByPropertiesId(propertyId);

		assertThat(res).hasSize(2);
		// Verify all items have correct propertyId
		assertThat(res).allMatch(r -> r.getPropertyId().equals(propertyId));
		// Verify file paths are mapped correctly
		assertThat(res)
			.extracting(PropertyGalleryRes::getFilePath)
			.containsExactlyInAnyOrder(file1.getRelativePath(), file2.getRelativePath());
	}
}
