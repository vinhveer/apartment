package com.qminh.apartment.repository;

import com.qminh.apartment.entity.Property;
import com.qminh.apartment.entity.PropertyArea;
import com.qminh.apartment.entity.PropertyGallery;
import com.qminh.apartment.entity.PropertyGalleryId;
import com.qminh.apartment.entity.PropertySaleInfo;
import com.qminh.apartment.entity.PropertyType;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.StoredFile;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.testsupport.PostgresTestContainer;
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
	@Autowired private StoredFileRepository storedFileRepository;

	private Property prepareProperty() {
		PropertyArea area = new PropertyArea();
		area.setAreaName("AreaPG");
		area.setAreaLink("area-pg");
		area = areaRepository.saveAndFlush(area);

		PropertyType type = new PropertyType();
		type.setTypeName("TypePG");
		type = typeRepository.saveAndFlush(type);

		Role saleRole = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role();
			r.setRoleName("SALE");
			return roleRepository.saveAndFlush(r);
		});

		User u = new User();
		u.setUsername("salePG");
		u.setEmail("salePG@example.com");
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
		return propertyRepository.saveAndFlush(p);
	}

	private StoredFile prepareFile(String name) {
		StoredFile f = new StoredFile();
		f.setFilePath("public/2025/01/" + Objects.requireNonNull(name));
		f.setCreatedAt(LocalDateTime.now());
		f.setUpdatedAt(LocalDateTime.now());
		return storedFileRepository.saveAndFlush(f);
	}

	@Test
	@DisplayName("findByProperty_PropertyId returns all gallery items for a property")
	void find_by_property_works() {
		Property p = prepareProperty();
		Long pid = Objects.requireNonNull(p.getPropertyId());
		StoredFile f1 = prepareFile("pg-1.jpg");
		StoredFile f2 = prepareFile("pg-2.jpg");

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
}



