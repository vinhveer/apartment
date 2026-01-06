package com.qminh.apartment.features.properties.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.features.accounts.domain.*;
import com.qminh.apartment.features.accounts.persistence.*;
import com.qminh.apartment.features.properties.domain.*;
import com.qminh.apartment.features.properties.dto.property.PropertyCreateReq;
import com.qminh.apartment.features.properties.dto.property.PropertyDetailFilterReq;
import com.qminh.apartment.features.properties.dto.property.PropertySearchReq;
import com.qminh.apartment.features.properties.dto.property.PropertyUpdateReq;
import com.qminh.apartment.features.properties.persistence.*;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class PropertyControllerIT extends PostgresTestContainer {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper mapper;
	@Autowired private PropertyTypeRepository typeRepo;
	@Autowired private PropertyAreaRepository areaRepo;
	@Autowired private RoleRepository roleRepo;
	@Autowired private UserRepository userRepo;
	@Autowired private PropertySaleInfoRepository saleInfoRepo;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private PropertyRepository propertyRepo;
	@Autowired private PropertyDefineDetailsRepository defineDetailsRepo;
	@Autowired private PropertyDetailsRepository detailsRepo;

	private Integer typeId;
	private Integer areaId;
	private Long saleUserId;

	@BeforeEach
	void setup() {
		PropertyType type = new PropertyType(); type.setTypeName("TypeA");
		typeId = typeRepo.saveAndFlush(type).getTypeId();
		PropertyArea area = new PropertyArea(); area.setAreaName("AreaA"); area.setAreaLink("area-a");
		areaId = areaRepo.saveAndFlush(area).getAreaId();
		Role saleRole = roleRepo.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role(); r.setRoleName("SALE"); return roleRepo.saveAndFlush(r);
		});
		User u = new User();
		u.setUsername("saleP");
		u.setEmail("saleP@example.com");
		u.setPassword(passwordEncoder.encode("123456"));
		u.setRole(saleRole);
		saleUserId = userRepo.saveAndFlush(u).getId();
		User managed = userRepo.findById(Objects.requireNonNull(saleUserId)).orElseThrow();
		PropertySaleInfo info = new PropertySaleInfo();
		info.setUser(managed);
		info.setFullName("Sale Person");
		info.setPhone("0900000000");
		saleInfoRepo.saveAndFlush(info);
	}

	@Test
	@DisplayName("POST/GET/PUT/DELETE /api/properties works and returns ApiResponse")
	void crud_flow() throws Exception {
		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("Prop A");
		req.setPrice(new BigDecimal("1000.00"));
		req.setDescription("desc");
		req.setTypeId(Objects.requireNonNull(typeId));
		req.setSaleUserId(Objects.requireNonNull(saleUserId));
		req.setAreaId(Objects.requireNonNull(areaId));
		req.setIsPublic(true);
		req.setIsForRent(false);

		String createRes = mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Create property successfully"))
			.andExpect(jsonPath("$.data.propertyId").exists())
			.andReturn().getResponse().getContentAsString();

		long id = mapper.readTree(createRes).path("data").path("propertyId").asLong();

		mockMvc.perform(get("/api/properties/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property detail"))
			.andExpect(jsonPath("$.data.title").value("Prop A"))
			.andExpect(jsonPath("$.data.createdAt").exists())
			.andExpect(jsonPath("$.data.updatedAt").exists());

		PropertyUpdateReq up = new PropertyUpdateReq();
		up.setTitle("Prop B");
		up.setPrice(new BigDecimal("2000.00"));
		up.setDescription("desc2");
		up.setTypeId(typeId);
		up.setSaleUserId(saleUserId);
		up.setAreaId(areaId);
		up.setIsPublic(false);
		up.setIsForRent(true);

		mockMvc.perform(put("/api/properties/{id}", id)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(up))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update property successfully"))
			.andExpect(jsonPath("$.data.title").value("Prop B"))
			.andExpect(jsonPath("$.data.isPublic").value(false))
			.andExpect(jsonPath("$.data.createdAt").exists())
			.andExpect(jsonPath("$.data.updatedAt").exists());

		mockMvc.perform(delete("/api/properties/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete property successfully"));
	}

	@Test
	@DisplayName("GET list returns meta and data; 404/400 cases")
	void list_and_error_cases() throws Exception {
		// create one valid property
		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("List A");
		req.setPrice(new BigDecimal("100.00"));
		req.setDescription("d");
		req.setTypeId(Objects.requireNonNull(typeId));
		req.setSaleUserId(Objects.requireNonNull(saleUserId));
		req.setAreaId(Objects.requireNonNull(areaId));
		req.setIsPublic(true);
		req.setIsForRent(false);
		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk());
		// list
		mockMvc.perform(get("/api/properties?page=0&size=5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meta.total").exists())
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content[0].createdAt").exists())
			.andExpect(jsonPath("$.data.content[0].updatedAt").exists());
		// 404 not found
		mockMvc.perform(get("/api/properties/{id}", 999999))
			.andExpect(status().isNotFound());
		PropertyUpdateReq up = new PropertyUpdateReq();
		up.setTitle("X");
		up.setPrice(new BigDecimal("1.00"));
		up.setDescription("d");
		up.setTypeId(Objects.requireNonNull(typeId));
		up.setSaleUserId(Objects.requireNonNull(saleUserId));
		up.setAreaId(Objects.requireNonNull(areaId));
		up.setIsPublic(false);
		up.setIsForRent(true);
		mockMvc.perform(put("/api/properties/{id}", 999999)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(up))))
			.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/properties/{id}", 999999))
			.andExpect(status().isNotFound());
		// 400 validation missing required fields
		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("advanced search basic flow")
	void advanced_search_basic_flow() throws Exception {
		PropertyCreateReq req1 = new PropertyCreateReq();
		req1.setTitle("Vinhome Central");
		req1.setPrice(new BigDecimal("2000000000.00"));
		req1.setDescription("Luxury");
		req1.setTypeId(Objects.requireNonNull(typeId));
		req1.setSaleUserId(Objects.requireNonNull(saleUserId));
		req1.setAreaId(Objects.requireNonNull(areaId));
		req1.setIsPublic(true);
		req1.setIsForRent(true);
		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req1))))
			.andExpect(status().isOk());

		PropertyCreateReq req2 = new PropertyCreateReq();
		req2.setTitle("Another Property");
		req2.setPrice(new BigDecimal("5000000000.00"));
		req2.setDescription("Normal");
		req2.setTypeId(Objects.requireNonNull(typeId));
		req2.setSaleUserId(Objects.requireNonNull(saleUserId));
		req2.setAreaId(Objects.requireNonNull(areaId));
		req2.setIsPublic(true);
		req2.setIsForRent(false);
		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req2))))
			.andExpect(status().isOk());

		PropertySearchReq searchReq = new PropertySearchReq();
		searchReq.setQ("vinhome");
		searchReq.setIsForRent(true);
		searchReq.setMinPrice(new BigDecimal("1000000000.00"));
		searchReq.setMaxPrice(new BigDecimal("3000000000.00"));

		mockMvc.perform(post("/api/properties/search?page=0&size=10")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(searchReq))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property search result"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content[0].title").value("Vinhome Central"))
			.andExpect(jsonPath("$.data.content[0].isForRent").value(true))
			.andExpect(jsonPath("$.meta.total").exists());
	}

	@Test
	@DisplayName("advanced search with details filters")
	void advanced_search_with_details_filters() throws Exception {
		PropertyDefineDetails areaDetail = new PropertyDefineDetails();
		areaDetail.setDetailName("Diện tích");
		areaDetail.setIsNumber(true);
		areaDetail.setUnit("m2");
		areaDetail = defineDetailsRepo.saveAndFlush(areaDetail);
		Integer areaDetailId = areaDetail.getDetailId();

		PropertyDefineDetails bedroomDetail = new PropertyDefineDetails();
		bedroomDetail.setDetailName("Số phòng ngủ");
		bedroomDetail.setIsNumber(true);
		bedroomDetail.setUnit("phòng");
		bedroomDetail = defineDetailsRepo.saveAndFlush(bedroomDetail);
		Integer bedroomDetailId = bedroomDetail.getDetailId();

		PropertyCreateReq req1 = new PropertyCreateReq();
		req1.setTitle("Prop A");
		req1.setPrice(new BigDecimal("1000000000.00"));
		req1.setDescription("d");
		req1.setTypeId(Objects.requireNonNull(typeId));
		req1.setSaleUserId(Objects.requireNonNull(saleUserId));
		req1.setAreaId(Objects.requireNonNull(areaId));
		req1.setIsPublic(true);
		req1.setIsForRent(false);
		String createRes1 = mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req1))))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		long propId1 = mapper.readTree(createRes1).path("data").path("propertyId").asLong();

		var entity1 = propertyRepo.findById(propId1).orElseThrow();
		PropertyDetails detail1 = new PropertyDetails();
		detail1.setId(new PropertyDetailsId(areaDetailId, propId1));
		detail1.setProperty(entity1);
		detail1.setDetail(areaDetail);
		detail1.setValue("80");
		detailsRepo.saveAndFlush(detail1);

		PropertyDetails detail2 = new PropertyDetails();
		detail2.setId(new PropertyDetailsId(bedroomDetailId, propId1));
		detail2.setProperty(entity1);
		detail2.setDetail(bedroomDetail);
		detail2.setValue("2");
		detailsRepo.saveAndFlush(detail2);

		PropertySearchReq searchReq = new PropertySearchReq();
		PropertyDetailFilterReq detailFilter1 = new PropertyDetailFilterReq();
		detailFilter1.setDetailId(areaDetailId);
		detailFilter1.setMinNumber(new BigDecimal("70"));
		PropertyDetailFilterReq detailFilter2 = new PropertyDetailFilterReq();
		detailFilter2.setDetailId(bedroomDetailId);
		detailFilter2.setNumber(new BigDecimal("2"));
		searchReq.setDetails(List.of(detailFilter1, detailFilter2));

		mockMvc.perform(post("/api/properties/search?page=0&size=10")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(searchReq))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property search result"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content[0].propertyId").value(propId1));
	}

	@Test
	@DisplayName("advanced search paging and sort")
	void advanced_search_paging_and_sort() throws Exception {
		for (int i = 0; i < 3; i++) {
			PropertyCreateReq req = new PropertyCreateReq();
			req.setTitle("Prop_" + i);
			req.setPrice(new BigDecimal((i + 1) * 1000000000.00));
			req.setDescription("d");
			req.setTypeId(Objects.requireNonNull(typeId));
			req.setSaleUserId(Objects.requireNonNull(saleUserId));
			req.setAreaId(Objects.requireNonNull(areaId));
			req.setIsPublic(true);
			req.setIsForRent(false);
			mockMvc.perform(post("/api/properties")
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
				.andExpect(status().isOk());
		}

		PropertySearchReq searchReq = new PropertySearchReq();

		mockMvc.perform(post("/api/properties/search?page=0&size=2&sort=price,asc")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(searchReq))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meta.page").value(0))
			.andExpect(jsonPath("$.meta.size").value(2))
			.andExpect(jsonPath("$.meta.total").exists())
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content[0].price").exists())
			.andExpect(jsonPath("$.data.content[1].price").exists());
	}

	@Test
	@DisplayName("advanced search empty body")
	void advanced_search_empty_body() throws Exception {
		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("Prop Empty");
		req.setPrice(new BigDecimal("1000000.00"));
		req.setDescription("d");
		req.setTypeId(Objects.requireNonNull(typeId));
		req.setSaleUserId(Objects.requireNonNull(saleUserId));
		req.setAreaId(Objects.requireNonNull(areaId));
		req.setIsPublic(true);
		req.setIsForRent(false);
		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk());

		PropertySearchReq searchReq = new PropertySearchReq();

		mockMvc.perform(post("/api/properties/search?page=0&size=10")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(searchReq))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property search result"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.meta.total").exists());
	}

	@Test
	@DisplayName("search with mode=list returns summary fields (typeName, areaName, saleDisplayName, salePhone)")
	void search_mode_list_returns_summary_fields() throws Exception {
		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("Prop Mode List");
		req.setPrice(new BigDecimal("1000000.00"));
		req.setDescription("d");
		req.setTypeId(Objects.requireNonNull(typeId));
		req.setSaleUserId(Objects.requireNonNull(saleUserId));
		req.setAreaId(Objects.requireNonNull(areaId));
		req.setIsPublic(true);
		req.setIsForRent(false);
		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk());

		PropertySearchReq searchReq = new PropertySearchReq();

		mockMvc.perform(post("/api/properties/search?page=0&size=10&mode=list")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(searchReq))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property search result"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content[0].typeName").value("TypeA"))
			.andExpect(jsonPath("$.data.content[0].areaName").value("AreaA"))
			.andExpect(jsonPath("$.data.content[0].salePhone").value("0900000000"))
			.andExpect(jsonPath("$.meta.total").exists());
	}

	@Test
	@DisplayName("search with mode=select returns full related data (type, area, saleInfo, galleries, details)")
	void search_mode_select_returns_full_data() throws Exception {
		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("Prop Mode Select");
		req.setPrice(new BigDecimal("2000000000.00"));
		req.setDescription("Luxury property");
		req.setTypeId(Objects.requireNonNull(typeId));
		req.setSaleUserId(Objects.requireNonNull(saleUserId));
		req.setAreaId(Objects.requireNonNull(areaId));
		req.setIsPublic(true);
		req.setIsForRent(true);
		String createRes = mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		long propId = mapper.readTree(createRes).path("data").path("propertyId").asLong();

		PropertySearchReq searchReq = new PropertySearchReq();
		searchReq.setQ("Mode Select");

		mockMvc.perform(post("/api/properties/search?page=0&size=10&mode=select")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(searchReq))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property search result (select)"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content[0].propertyId").value(propId))
			.andExpect(jsonPath("$.data.content[0].title").value("Prop Mode Select"))
			.andExpect(jsonPath("$.data.content[0].type.typeId").value(typeId))
			.andExpect(jsonPath("$.data.content[0].type.typeName").value("TypeA"))
			.andExpect(jsonPath("$.data.content[0].area.areaId").value(areaId))
			.andExpect(jsonPath("$.data.content[0].area.areaName").value("AreaA"))
			.andExpect(jsonPath("$.data.content[0].area.areaLink").value("area-a"))
			.andExpect(jsonPath("$.data.content[0].saleInfo.userId").value(saleUserId))
			.andExpect(jsonPath("$.data.content[0].saleInfo.phone").value("0900000000"))
			.andExpect(jsonPath("$.data.content[0].details").isArray())
			.andExpect(jsonPath("$.data.content[0].galleries").isArray())
			.andExpect(jsonPath("$.meta.total").exists());
	}

	@Test
	@DisplayName("search with mode=select and filters works correctly")
	void search_mode_select_with_filters() throws Exception {
		PropertyCreateReq req1 = new PropertyCreateReq();
		req1.setTitle("Vinhome Select");
		req1.setPrice(new BigDecimal("2000000000.00"));
		req1.setDescription("Luxury");
		req1.setTypeId(Objects.requireNonNull(typeId));
		req1.setSaleUserId(Objects.requireNonNull(saleUserId));
		req1.setAreaId(Objects.requireNonNull(areaId));
		req1.setIsPublic(true);
		req1.setIsForRent(true);
		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req1))))
			.andExpect(status().isOk());

		PropertyCreateReq req2 = new PropertyCreateReq();
		req2.setTitle("Another Select");
		req2.setPrice(new BigDecimal("5000000000.00"));
		req2.setDescription("Normal");
		req2.setTypeId(Objects.requireNonNull(typeId));
		req2.setSaleUserId(Objects.requireNonNull(saleUserId));
		req2.setAreaId(Objects.requireNonNull(areaId));
		req2.setIsPublic(true);
		req2.setIsForRent(false);
		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req2))))
			.andExpect(status().isOk());

		PropertySearchReq searchReq = new PropertySearchReq();
		searchReq.setQ("vinhome");
		searchReq.setIsForRent(true);
		searchReq.setMinPrice(new BigDecimal("1000000000.00"));
		searchReq.setMaxPrice(new BigDecimal("3000000000.00"));

		mockMvc.perform(post("/api/properties/search?page=0&size=10&mode=select")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(searchReq))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property search result (select)"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content[0].title").value("Vinhome Select"))
			.andExpect(jsonPath("$.data.content[0].type.typeName").value("TypeA"))
			.andExpect(jsonPath("$.data.content[0].area.areaName").value("AreaA"))
			.andExpect(jsonPath("$.data.content[0].saleInfo.phone").value("0900000000"))
			.andExpect(jsonPath("$.meta.total").value(1));
	}
}

