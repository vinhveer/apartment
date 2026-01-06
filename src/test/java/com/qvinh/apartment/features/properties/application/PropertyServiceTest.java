package com.qvinh.apartment.features.properties.application;

import com.qvinh.apartment.features.properties.application.impl.PropertyService;
import com.qvinh.apartment.features.accounts.domain.*;
import com.qvinh.apartment.features.accounts.persistence.*;
import com.qvinh.apartment.features.properties.domain.*;
import com.qvinh.apartment.features.properties.dto.property.PropertyCreateReq;
import com.qvinh.apartment.features.properties.dto.property.PropertyDetailFilterReq;
import com.qvinh.apartment.features.properties.dto.property.PropertySearchReq;
import com.qvinh.apartment.features.properties.dto.property.PropertyUpdateReq;
import com.qvinh.apartment.features.properties.persistence.*;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PropertyServiceTest extends PostgresTestContainer {

	@Autowired private PropertyService service;
	@Autowired private PropertyTypeRepository typeRepository;
	@Autowired private PropertyAreaRepository areaRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private PropertySaleInfoRepository saleInfoRepository;
	@Autowired private PropertyRepository propertyRepository;
	@Autowired private PropertyDefineDetailsRepository defineDetailsRepository;
	@Autowired private PropertyDetailsRepository detailsRepository;

	private Integer typeId;
	private Integer areaId;
	private Long saleUserId;

	@BeforeEach
	void setup() {
		PropertyType type = new PropertyType(); type.setTypeName("TypeS");
		typeId = typeRepository.saveAndFlush(type).getTypeId();
		PropertyArea area = new PropertyArea(); area.setAreaName("AreaS"); area.setAreaLink("area-s");
		areaId = areaRepository.saveAndFlush(area).getAreaId();
		Role saleRole = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("SALE"); return roleRepository.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("saleS");
		u.setEmail("saleS@example.com");
		u.setPassword("x");
		u.setRole(saleRole);
		saleUserId = userRepository.saveAndFlush(u).getId();
		PropertySaleInfo info = new PropertySaleInfo();
		info.setUser(u);
		info.setFullName("Sale S");
		info.setPhone("0900");
		saleInfoRepository.saveAndFlush(info);
	}

	@Test
	@Transactional
	@DisplayName("Property create/get/update/delete works")
	void crud_flow() {
		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("T1");
		req.setPrice(new BigDecimal("100.00"));
		req.setDescription("d");
		req.setTypeId(typeId);
		req.setSaleUserId(saleUserId);
		req.setAreaId(areaId);
		req.setIsPublic(true);
		req.setIsForRent(false);
		var created = service.create(req);
		assertThat(created.getPropertyId()).isNotNull();
		assertThat(created.getCreatedAt()).isNotNull();
		assertThat(created.getUpdatedAt()).isNotNull();

		var got = service.get(created.getPropertyId());
		assertThat(got.getTitle()).isEqualTo("T1");
		assertThat(got.getCreatedAt()).isNotNull();
		assertThat(got.getUpdatedAt()).isNotNull();

		PropertyUpdateReq up = new PropertyUpdateReq();
		up.setTitle("T2");
		up.setPrice(new BigDecimal("200.00"));
		up.setDescription("d2");
		up.setTypeId(typeId);
		up.setSaleUserId(saleUserId);
		up.setAreaId(areaId);
		up.setIsPublic(false);
		up.setIsForRent(true);
		var updated = service.update(created.getPropertyId(), up);
		assertThat(updated.getTitle()).isEqualTo("T2");
		assertThat(updated.getCreatedAt()).isNotNull();
		assertThat(updated.getUpdatedAt()).isNotNull();
		assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());
	}

	@Test
	@Transactional
	@DisplayName("delete then get throws not found")
	void delete_then_notfound() {
		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("TD");
		req.setPrice(new BigDecimal("150.00"));
		req.setDescription("d");
		req.setTypeId(typeId);
		req.setSaleUserId(saleUserId);
		req.setAreaId(areaId);
		req.setIsPublic(false);
		req.setIsForRent(true);
		var created = service.create(req);
		Long deletedId = created.getPropertyId();
		service.delete(deletedId);
		assertThatThrownBy(() -> service.get(deletedId))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@Transactional
	@DisplayName("list(Pageable) supports pagination and sort; null pageable throws NPE")
	void list_pageable_and_null() {
		for (int i = 0; i < 5; i++) {
			PropertyCreateReq req = new PropertyCreateReq();
			req.setTitle("Tx_" + System.nanoTime() + "_" + i);
			req.setPrice(new BigDecimal("100.00"));
			req.setDescription("d");
			req.setTypeId(typeId);
			req.setSaleUserId(saleUserId);
			req.setAreaId(areaId);
			req.setIsPublic(true);
			req.setIsForRent(false);
			service.create(req);
		}
		Pageable p = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "propertyId"));
		var page = service.list(p);
		assertThat(page.getContent()).hasSizeLessThanOrEqualTo(3);
		assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(5);
		if (!page.getContent().isEmpty()) {
			assertThat(page.getContent().get(0).getCreatedAt()).isNotNull();
			assertThat(page.getContent().get(0).getUpdatedAt()).isNotNull();
		}
		assertThatThrownBy(() -> service.list(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@Transactional
	@DisplayName("not found cases: get/update/delete with non-existing id")
	void not_found_cases() {
		assertThatThrownBy(() -> service.get(999999)).isInstanceOf(ResourceNotFoundException.class);
		PropertyUpdateReq up = new PropertyUpdateReq();
		up.setTitle("N");
		up.setPrice(new BigDecimal("1.00"));
		up.setDescription("n");
		up.setTypeId(typeId);
		up.setSaleUserId(saleUserId);
		up.setAreaId(areaId);
		up.setIsPublic(true);
		up.setIsForRent(false);
		assertThatThrownBy(() -> service.update(999999, up)).isInstanceOf(ResourceNotFoundException.class);
		assertThatThrownBy(() -> service.delete(999999)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@Transactional
	@DisplayName("search by q filters on multiple text fields")
	void search_by_q_filters_on_multiple_text_fields() {
		PropertyCreateReq req1 = new PropertyCreateReq();
		req1.setTitle("Vinhome Central Park");
		req1.setPrice(new BigDecimal("5000000000.00"));
		req1.setDescription("Luxury apartment");
		req1.setTypeId(typeId);
		req1.setSaleUserId(saleUserId);
		req1.setAreaId(areaId);
		req1.setIsPublic(true);
		req1.setIsForRent(false);
		var prop1 = service.create(req1);

		PropertyCreateReq req2 = new PropertyCreateReq();
		req2.setTitle("Another Property");
		req2.setPrice(new BigDecimal("3000000000.00"));
		req2.setDescription("Normal");
		req2.setTypeId(typeId);
		req2.setSaleUserId(saleUserId);
		req2.setAreaId(areaId);
		req2.setIsPublic(true);
		req2.setIsForRent(false);
		service.create(req2);

		PropertySearchReq searchReq = new PropertySearchReq();
		searchReq.setQ("vinhome");
		Pageable pageable = PageRequest.of(0, 10);
		var result = service.search(searchReq, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getPropertyId()).isEqualTo(prop1.getPropertyId());
		assertThat(result.getContent().get(0).getTitle()).containsIgnoringCase("vinhome");
	}

	@Test
	@Transactional
	@DisplayName("search combines fixed fields with and logic")
	void search_combines_fixed_fields_with_and_logic() {
		PropertyCreateReq req1 = new PropertyCreateReq();
		req1.setTitle("Prop A");
		req1.setPrice(new BigDecimal("2000000000.00"));
		req1.setDescription("d");
		req1.setTypeId(typeId);
		req1.setSaleUserId(saleUserId);
		req1.setAreaId(areaId);
		req1.setIsPublic(true);
		req1.setIsForRent(true);
		var prop1 = service.create(req1);

		PropertyCreateReq req2 = new PropertyCreateReq();
		req2.setTitle("Prop B");
		req2.setPrice(new BigDecimal("4000000000.00"));
		req2.setDescription("d");
		req2.setTypeId(typeId);
		req2.setSaleUserId(saleUserId);
		req2.setAreaId(areaId);
		req2.setIsPublic(true);
		req2.setIsForRent(false);
		service.create(req2);

		PropertySearchReq searchReq = new PropertySearchReq();
		searchReq.setIsForRent(true);
		searchReq.setMinPrice(new BigDecimal("1000000000.00"));
		searchReq.setMaxPrice(new BigDecimal("3000000000.00"));
		Pageable pageable = PageRequest.of(0, 10);
		var result = service.search(searchReq, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getPropertyId()).isEqualTo(prop1.getPropertyId());
		assertThat(result.getContent().get(0).getIsForRent()).isTrue();
		assertThat(result.getContent().get(0).getPrice()).isBetween(new BigDecimal("1000000000.00"), new BigDecimal("3000000000.00"));
	}

	@Test
	@Transactional
	@DisplayName("search by created and updated range")
	void search_by_created_and_updated_range() {
		LocalDateTime baseTime = LocalDateTime.now().minusDays(5);
		PropertyCreateReq req1 = new PropertyCreateReq();
		req1.setTitle("Prop A");
		req1.setPrice(new BigDecimal("1000000000.00"));
		req1.setDescription("d");
		req1.setTypeId(typeId);
		req1.setSaleUserId(saleUserId);
		req1.setAreaId(areaId);
		req1.setIsPublic(true);
		req1.setIsForRent(false);
		var prop1 = service.create(req1);
		var entity1 = propertyRepository.findById(Objects.requireNonNull(prop1.getPropertyId())).orElseThrow();
		entity1.setCreatedAt(baseTime.plusDays(2));
		entity1.setUpdatedAt(baseTime.plusDays(2));
		propertyRepository.saveAndFlush(entity1);

		PropertyCreateReq req2 = new PropertyCreateReq();
		req2.setTitle("Prop B");
		req2.setPrice(new BigDecimal("2000000000.00"));
		req2.setDescription("d");
		req2.setTypeId(typeId);
		req2.setSaleUserId(saleUserId);
		req2.setAreaId(areaId);
		req2.setIsPublic(true);
		req2.setIsForRent(false);
		var prop2 = service.create(req2);
		var entity2 = propertyRepository.findById(Objects.requireNonNull(prop2.getPropertyId())).orElseThrow();
		entity2.setCreatedAt(baseTime.plusDays(10));
		entity2.setUpdatedAt(baseTime.plusDays(10));
		propertyRepository.saveAndFlush(entity2);

		PropertySearchReq searchReq = new PropertySearchReq();
		searchReq.setCreatedFrom(baseTime.plusDays(1));
		searchReq.setCreatedTo(baseTime.plusDays(5));
		// thêm điều kiện title để tránh ảnh hưởng bởi dữ liệu từ test khác
		searchReq.setTitle("Prop A");
		Pageable pageable = PageRequest.of(0, 10);
		var result = service.search(searchReq, pageable);

		assertThat(result.getContent())
			.extracting(r -> r.getPropertyId())
			.contains(prop1.getPropertyId());
	}

	@Test
	@Transactional
	@DisplayName("search by details numeric and text")
	void search_by_details_numeric_and_text() {
		PropertyDefineDetails areaDetail = new PropertyDefineDetails();
		areaDetail.setDetailName("Diện tích");
		areaDetail.setIsNumber(true);
		areaDetail.setUnit("m2");
		areaDetail = defineDetailsRepository.saveAndFlush(areaDetail);
		Integer areaDetailId = areaDetail.getDetailId();

		PropertyDefineDetails bedroomDetail = new PropertyDefineDetails();
		bedroomDetail.setDetailName("Số phòng ngủ");
		bedroomDetail.setIsNumber(true);
		bedroomDetail.setUnit("phòng");
		bedroomDetail = defineDetailsRepository.saveAndFlush(bedroomDetail);
		Integer bedroomDetailId = bedroomDetail.getDetailId();

		PropertyDefineDetails directionDetail = new PropertyDefineDetails();
		directionDetail.setDetailName("Hướng nhà");
		directionDetail.setIsNumber(false);
		directionDetail = defineDetailsRepository.saveAndFlush(directionDetail);
		Integer directionDetailId = directionDetail.getDetailId();

		PropertyCreateReq req1 = new PropertyCreateReq();
		req1.setTitle("Prop A");
		req1.setPrice(new BigDecimal("1000000000.00"));
		req1.setDescription("d");
		req1.setTypeId(typeId);
		req1.setSaleUserId(saleUserId);
		req1.setAreaId(areaId);
		req1.setIsPublic(true);
		req1.setIsForRent(false);
		var prop1 = service.create(req1);
		var entity1 = propertyRepository.findById(Objects.requireNonNull(prop1.getPropertyId())).orElseThrow();

		PropertyDetails detail1 = new PropertyDetails();
		detail1.setId(new PropertyDetailsId(areaDetailId, prop1.getPropertyId()));
		detail1.setProperty(entity1);
		detail1.setDetail(areaDetail);
		detail1.setValue("80");
		detailsRepository.saveAndFlush(detail1);

		PropertyDetails detail2 = new PropertyDetails();
		detail2.setId(new PropertyDetailsId(bedroomDetailId, prop1.getPropertyId()));
		detail2.setProperty(entity1);
		detail2.setDetail(bedroomDetail);
		detail2.setValue("2");
		detailsRepository.saveAndFlush(detail2);

		PropertyDetails detail3 = new PropertyDetails();
		detail3.setId(new PropertyDetailsId(directionDetailId, prop1.getPropertyId()));
		detail3.setProperty(entity1);
		detail3.setDetail(directionDetail);
		detail3.setValue("Đông Nam");
		detailsRepository.saveAndFlush(detail3);

		PropertyCreateReq req2 = new PropertyCreateReq();
		req2.setTitle("Prop B");
		req2.setPrice(new BigDecimal("2000000000.00"));
		req2.setDescription("d");
		req2.setTypeId(typeId);
		req2.setSaleUserId(saleUserId);
		req2.setAreaId(areaId);
		req2.setIsPublic(true);
		req2.setIsForRent(false);
		service.create(req2);

		PropertySearchReq searchReq = new PropertySearchReq();
		PropertyDetailFilterReq detailFilter1 = new PropertyDetailFilterReq();
		detailFilter1.setDetailId(areaDetailId);
		detailFilter1.setMinNumber(new BigDecimal("70"));
		PropertyDetailFilterReq detailFilter2 = new PropertyDetailFilterReq();
		detailFilter2.setDetailId(bedroomDetailId);
		detailFilter2.setNumber(new BigDecimal("2"));
		PropertyDetailFilterReq detailFilter3 = new PropertyDetailFilterReq();
		detailFilter3.setDetailId(directionDetailId);
		detailFilter3.setText("Đông");
		searchReq.setDetails(List.of(detailFilter1, detailFilter2, detailFilter3));

		Pageable pageable = PageRequest.of(0, 10);
		var result = service.search(searchReq, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getPropertyId()).isEqualTo(prop1.getPropertyId());
	}

	@Test
	@Transactional
	@DisplayName("search empty body returns all with paging")
	void search_empty_body_returns_all_with_paging() {
		for (int i = 0; i < 5; i++) {
			PropertyCreateReq req = new PropertyCreateReq();
			req.setTitle("Prop_" + System.nanoTime() + "_" + i);
			req.setPrice(new BigDecimal("1000000.00"));
			req.setDescription("d");
			req.setTypeId(typeId);
			req.setSaleUserId(saleUserId);
			req.setAreaId(areaId);
			req.setIsPublic(true);
			req.setIsForRent(false);
			service.create(req);
		}

		PropertySearchReq searchReq = new PropertySearchReq();
		Pageable pageable = PageRequest.of(0, 3);
		var result = service.search(searchReq, pageable);

		assertThat(result.getContent()).hasSizeLessThanOrEqualTo(3);
		assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(5);
	}

	@Test
	@Transactional
	@DisplayName("search null pageable throws npe")
	void search_null_pageable_throws_npe() {
		PropertySearchReq searchReq = new PropertySearchReq();
		assertThatThrownBy(() -> service.search(searchReq, null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@Transactional
	@DisplayName("searchFull returns PropertySelectRes with full related data")
	void searchFull_returns_full_related_data() {
		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("Prop Full Test");
		req.setPrice(new BigDecimal("2000000000.00"));
		req.setDescription("Full test property");
		req.setTypeId(typeId);
		req.setSaleUserId(saleUserId);
		req.setAreaId(areaId);
		req.setIsPublic(true);
		req.setIsForRent(true);
		var created = service.create(req);

		PropertySearchReq searchReq = new PropertySearchReq();
		searchReq.setQ("Full Test");
		Pageable pageable = PageRequest.of(0, 10);
		var result = service.searchFull(searchReq, pageable);

		assertThat(result.getContent()).hasSize(1);
		var selectRes = result.getContent().get(0);
		assertThat(selectRes.getPropertyId()).isEqualTo(created.getPropertyId());
		assertThat(selectRes.getTitle()).isEqualTo("Prop Full Test");
		assertThat(selectRes.getType()).isNotNull();
		assertThat(selectRes.getType().getTypeId()).isEqualTo(typeId);
		assertThat(selectRes.getType().getTypeName()).isEqualTo("TypeS");
		assertThat(selectRes.getArea()).isNotNull();
		assertThat(selectRes.getArea().getAreaId()).isEqualTo(areaId);
		assertThat(selectRes.getArea().getAreaName()).isEqualTo("AreaS");
		assertThat(selectRes.getArea().getAreaLink()).isEqualTo("area-s");
		assertThat(selectRes.getSaleInfo()).isNotNull();
		assertThat(selectRes.getSaleInfo().getUserId()).isEqualTo(saleUserId);
		assertThat(selectRes.getSaleInfo().getPhone()).isEqualTo("0900");
		assertThat(selectRes.getDetails()).isNotNull();
		assertThat(selectRes.getGalleries()).isNotNull();
	}

	@Test
	@Transactional
	@DisplayName("searchFull with filters returns correct results")
	void searchFull_with_filters_returns_correct_results() {
		PropertyCreateReq req1 = new PropertyCreateReq();
		req1.setTitle("Vinhome Full");
		req1.setPrice(new BigDecimal("2000000000.00"));
		req1.setDescription("Luxury");
		req1.setTypeId(typeId);
		req1.setSaleUserId(saleUserId);
		req1.setAreaId(areaId);
		req1.setIsPublic(true);
		req1.setIsForRent(true);
		service.create(req1);

		PropertyCreateReq req2 = new PropertyCreateReq();
		req2.setTitle("Another Full");
		req2.setPrice(new BigDecimal("5000000000.00"));
		req2.setDescription("Normal");
		req2.setTypeId(typeId);
		req2.setSaleUserId(saleUserId);
		req2.setAreaId(areaId);
		req2.setIsPublic(true);
		req2.setIsForRent(false);
		service.create(req2);

		PropertySearchReq searchReq = new PropertySearchReq();
		searchReq.setQ("vinhome");
		searchReq.setIsForRent(true);
		searchReq.setMinPrice(new BigDecimal("1000000000.00"));
		searchReq.setMaxPrice(new BigDecimal("3000000000.00"));
		Pageable pageable = PageRequest.of(0, 10);
		var result = service.searchFull(searchReq, pageable);

		assertThat(result.getContent()).hasSize(1);
		var selectRes = result.getContent().get(0);
		assertThat(selectRes.getTitle()).isEqualTo("Vinhome Full");
		assertThat(selectRes.getType()).isNotNull();
		assertThat(selectRes.getArea()).isNotNull();
		assertThat(selectRes.getSaleInfo()).isNotNull();
	}

	@Test
	@Transactional
	@DisplayName("searchFull null pageable throws npe")
	void searchFull_null_pageable_throws_npe() {
		PropertySearchReq searchReq = new PropertySearchReq();
		assertThatThrownBy(() -> service.searchFull(searchReq, null))
			.isInstanceOf(NullPointerException.class);
	}
}
