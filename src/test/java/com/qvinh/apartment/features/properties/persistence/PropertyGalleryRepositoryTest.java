package com.qvinh.apartment.features.properties.persistence;

import com.qvinh.apartment.features.accounts.domain.PropertySaleInfo;
import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.persistence.PropertySaleInfoRepository;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.features.files.domain.StoredFileMeta;
import com.qvinh.apartment.features.files.persistence.StoredFileMetaRepository;
import com.qvinh.apartment.features.properties.domain.Property;
import com.qvinh.apartment.features.properties.domain.PropertyArea;
import com.qvinh.apartment.features.properties.domain.PropertyGallery;
import com.qvinh.apartment.features.properties.domain.PropertyGalleryId;
import com.qvinh.apartment.features.properties.domain.PropertyType;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PropertyGalleryRepositoryTest extends PostgresTestContainer {

	@Autowired private PropertyGalleryRepository galleryRepository;
	@Autowired private PropertyRepository propertyRepository;
	@Autowired private PropertyAreaRepository areaRepository;
	@Autowired private PropertyTypeRepository typeRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private PropertySaleInfoRepository saleInfoRepository;
	@Autowired private StoredFileMetaRepository storedFileMetaRepository;

	private Property prepareProperty() {
		long uniqueId = System.nanoTime();
		PropertyArea area = new PropertyArea();
		area.setAreaName("AreaPG_" + uniqueId);
		area.setAreaLink("area-pg-" + uniqueId);
		area = areaRepository.saveAndFlush(area);

		PropertyType type = new PropertyType();
		type.setTypeName("TypePG_" + uniqueId);
		type = typeRepository.saveAndFlush(type);

		Role saleRole = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role();
			r.setRoleName("SALE");
			return roleRepository.saveAndFlush(r);
		});

		User u = new User();
		u.setUsername("salePG_" + uniqueId);
		u.setEmail("salePG_" + uniqueId + "@example.com");
		u.setPassword("x");
		u.setRole(saleRole);
		u = userRepository.saveAndFlush(u);

		PropertySaleInfo info = new PropertySaleInfo();
		info.setUser(u);
		info.setFullName("Sale PG");
		info.setPhone("0900");
		info = saleInfoRepository.saveAndFlush(info);

		Property p = new Property();
		p.setTitle("PG Prop");
		p.setPrice(new BigDecimal("100.00"));
		p.setDescription("d");
		p.setType(type);
		p.setSaleInfo(info);
		p.setArea(area);
		p.setIsPublic(Boolean.TRUE);
		p.setIsForRent(Boolean.FALSE);
		return propertyRepository.saveAndFlush(p);
	}

	private StoredFileMeta prepareFile(String name) {
		StoredFileMeta f = new StoredFileMeta();
		f.setOriginalName(Objects.requireNonNull(name));
		f.setStoredName("stored_" + System.nanoTime() + "_" + name);
		f.setExt("jpg");
		f.setMimeType("image/jpeg");
		f.setSizeBytes(1024L);
		String uniqueSha = String.format("%064x", System.nanoTime() + System.currentTimeMillis() + name.hashCode());
		f.setSha256(uniqueSha);
		f.setAccessLevel("PUBLIC");
		f.setLocation("LOCAL");
		f.setRelativePath("public/2025/01/" + name);
		f.setCreatedAt(LocalDateTime.now());
		f.setUpdatedAt(LocalDateTime.now());
		return storedFileMetaRepository.saveAndFlush(f);
	}

	@Test
	@DisplayName("findByProperty_PropertyId returns all gallery items for a property")
	void find_by_property_works() {
		Property p = prepareProperty();
		Long pid = Objects.requireNonNull(p.getPropertyId());
		StoredFileMeta f1 = prepareFile("pg-1.jpg");
		StoredFileMeta f2 = prepareFile("pg-2.jpg");

		PropertyGallery g1 = new PropertyGallery();
		g1.setId(new PropertyGalleryId(pid, Objects.requireNonNull(f1.getFileId())));
		g1.setProperty(p);
		g1.setFile(f1);
		g1.setCreatedAt(LocalDateTime.now());
		g1.setUpdatedAt(LocalDateTime.now());

		PropertyGallery g2 = new PropertyGallery();
		g2.setId(new PropertyGalleryId(pid, Objects.requireNonNull(f2.getFileId())));
		g2.setProperty(p);
		g2.setFile(f2);
		g2.setCreatedAt(LocalDateTime.now());
		g2.setUpdatedAt(LocalDateTime.now());

		galleryRepository.saveAndFlush(g1);
		galleryRepository.saveAndFlush(g2);

		java.util.List<PropertyGallery> list = java.util.List.copyOf(galleryRepository.findByProperty_PropertyId(pid));
		assertThat(list).hasSize(2);
		assertThat(list)
			.extracting(it -> it.getFile().getFileId())
			.containsExactlyInAnyOrder(f1.getFileId(), f2.getFileId());
	}

	@Test
	@DisplayName("findByProperty_PropertyId returns empty list when property has no gallery items")
	void find_by_property_empty() {
		Property p = prepareProperty();
		Long pid = Objects.requireNonNull(p.getPropertyId());

		java.util.List<PropertyGallery> list = java.util.List.copyOf(galleryRepository.findByProperty_PropertyId(pid));
		assertThat(list).isEmpty();
	}

	@Test
	@DisplayName("findByProperty_PropertyId returns only items for the specified property")
	void find_by_property_isolates_properties() {
		Property p1 = prepareProperty();
		Property p2 = prepareProperty();
		Long pid1 = Objects.requireNonNull(p1.getPropertyId());
		Long pid2 = Objects.requireNonNull(p2.getPropertyId());

		StoredFileMeta f1 = prepareFile("pg-p1-1.jpg");
		StoredFileMeta f2 = prepareFile("pg-p1-2.jpg");
		StoredFileMeta f3 = prepareFile("pg-p2-1.jpg");

		PropertyGallery g1 = new PropertyGallery();
		g1.setId(new PropertyGalleryId(pid1, Objects.requireNonNull(f1.getFileId())));
		g1.setProperty(p1);
		g1.setFile(f1);
		g1.setCreatedAt(LocalDateTime.now());
		g1.setUpdatedAt(LocalDateTime.now());

		PropertyGallery g2 = new PropertyGallery();
		g2.setId(new PropertyGalleryId(pid1, Objects.requireNonNull(f2.getFileId())));
		g2.setProperty(p1);
		g2.setFile(f2);
		g2.setCreatedAt(LocalDateTime.now());
		g2.setUpdatedAt(LocalDateTime.now());

		PropertyGallery g3 = new PropertyGallery();
		g3.setId(new PropertyGalleryId(pid2, Objects.requireNonNull(f3.getFileId())));
		g3.setProperty(p2);
		g3.setFile(f3);
		g3.setCreatedAt(LocalDateTime.now());
		g3.setUpdatedAt(LocalDateTime.now());

		galleryRepository.saveAndFlush(g1);
		galleryRepository.saveAndFlush(g2);
		galleryRepository.saveAndFlush(g3);

		java.util.List<PropertyGallery> list1 = java.util.List.copyOf(galleryRepository.findByProperty_PropertyId(pid1));
		assertThat(list1).hasSize(2);
		assertThat(list1)
			.extracting(it -> it.getFile().getFileId())
			.containsExactlyInAnyOrder(f1.getFileId(), f2.getFileId());

		java.util.List<PropertyGallery> list2 = java.util.List.copyOf(galleryRepository.findByProperty_PropertyId(pid2));
		assertThat(list2).hasSize(1);
		assertThat(list2.get(0).getFile().getFileId()).isEqualTo(f3.getFileId());
	}

	@Test
	@DisplayName("save PropertyGallery with composite key works correctly")
	void save_with_composite_key() {
		Property p = prepareProperty();
		Long pid = Objects.requireNonNull(p.getPropertyId());
		StoredFileMeta f = prepareFile("pg-save.jpg");

		PropertyGallery g = new PropertyGallery();
		g.setId(new PropertyGalleryId(pid, Objects.requireNonNull(f.getFileId())));
		g.setProperty(p);
		g.setFile(f);
		LocalDateTime now = LocalDateTime.now();
		g.setCreatedAt(now);
		g.setUpdatedAt(now);

		PropertyGallery saved = galleryRepository.saveAndFlush(g);
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getId().getPropertyId()).isEqualTo(pid);
		assertThat(saved.getId().getFileId()).isEqualTo(f.getFileId());
		assertThat(saved.getProperty().getPropertyId()).isEqualTo(pid);
		assertThat(saved.getFile().getFileId()).isEqualTo(f.getFileId());
	}

	@Test
	@DisplayName("existsById returns true when gallery item exists")
	void exists_by_id_true() {
		Property p = prepareProperty();
		Long pid = Objects.requireNonNull(p.getPropertyId());
		StoredFileMeta f = prepareFile("pg-exists.jpg");

		PropertyGallery g = new PropertyGallery();
		g.setId(new PropertyGalleryId(pid, Objects.requireNonNull(f.getFileId())));
		g.setProperty(p);
		g.setFile(f);
		g.setCreatedAt(LocalDateTime.now());
		g.setUpdatedAt(LocalDateTime.now());
		galleryRepository.saveAndFlush(g);

		PropertyGalleryId id = new PropertyGalleryId(pid, f.getFileId());
		assertThat(galleryRepository.existsById(id)).isTrue();
	}

	@Test
	@DisplayName("existsById returns false when gallery item does not exist")
	void exists_by_id_false() {
		Property p = prepareProperty();
		Long pid = Objects.requireNonNull(p.getPropertyId());
		StoredFileMeta f = prepareFile("pg-not-exists.jpg");

		PropertyGalleryId id = new PropertyGalleryId(pid, Objects.requireNonNull(f.getFileId()));
		assertThat(galleryRepository.existsById(id)).isFalse();
	}

	@Test
	@DisplayName("delete PropertyGallery removes the item")
	void delete_gallery_item() {
		Property p = prepareProperty();
		Long pid = Objects.requireNonNull(p.getPropertyId());
		StoredFileMeta f = prepareFile("pg-delete.jpg");

		PropertyGallery g = new PropertyGallery();
		g.setId(new PropertyGalleryId(pid, Objects.requireNonNull(f.getFileId())));
		g.setProperty(p);
		g.setFile(f);
		g.setCreatedAt(LocalDateTime.now());
		g.setUpdatedAt(LocalDateTime.now());
		galleryRepository.saveAndFlush(g);

		PropertyGalleryId id = new PropertyGalleryId(pid, f.getFileId());
		galleryRepository.deleteById(id);

		assertThat(galleryRepository.existsById(id)).isFalse();
		java.util.List<PropertyGallery> list = java.util.List.copyOf(galleryRepository.findByProperty_PropertyId(pid));
		assertThat(list).isEmpty();
	}

	@Test
	@DisplayName("same file can be added to different properties")
	void same_file_different_properties() {
		Property p1 = prepareProperty();
		Property p2 = prepareProperty();
		Long pid1 = Objects.requireNonNull(p1.getPropertyId());
		Long pid2 = Objects.requireNonNull(p2.getPropertyId());

		StoredFileMeta f = prepareFile("pg-shared.jpg");

		PropertyGallery g1 = new PropertyGallery();
		g1.setId(new PropertyGalleryId(pid1, Objects.requireNonNull(f.getFileId())));
		g1.setProperty(p1);
		g1.setFile(f);
		g1.setCreatedAt(LocalDateTime.now());
		g1.setUpdatedAt(LocalDateTime.now());

		PropertyGallery g2 = new PropertyGallery();
		g2.setId(new PropertyGalleryId(pid2, Objects.requireNonNull(f.getFileId())));
		g2.setProperty(p2);
		g2.setFile(f);
		g2.setCreatedAt(LocalDateTime.now());
		g2.setUpdatedAt(LocalDateTime.now());

		galleryRepository.saveAndFlush(g1);
		galleryRepository.saveAndFlush(g2);

		java.util.List<PropertyGallery> list1 = java.util.List.copyOf(galleryRepository.findByProperty_PropertyId(pid1));
		assertThat(list1).hasSize(1);
		assertThat(list1.get(0).getFile().getFileId()).isEqualTo(f.getFileId());

		java.util.List<PropertyGallery> list2 = java.util.List.copyOf(galleryRepository.findByProperty_PropertyId(pid2));
		assertThat(list2).hasSize(1);
		assertThat(list2.get(0).getFile().getFileId()).isEqualTo(f.getFileId());
	}
}


