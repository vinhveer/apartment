package com.qminh.apartment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.dto.property.PropertyCreateReq;
import com.qminh.apartment.dto.property.PropertyUpdateReq;
import com.qminh.apartment.entity.PropertyArea;
import com.qminh.apartment.entity.PropertySaleInfo;
import com.qminh.apartment.entity.PropertyType;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.repository.PropertyAreaRepository;
import com.qminh.apartment.repository.PropertyTypeRepository;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.repository.PropertySaleInfoRepository;
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
			.andExpect(jsonPath("$.data.title").value("Prop A"));

		PropertyUpdateReq up = new PropertyUpdateReq();
		up.setTitle("Prop B");
		up.setPrice(new BigDecimal("2000.00"));
		up.setDescription("desc2");
		up.setTypeId(typeId);
		up.setSaleUserId(saleUserId);
		up.setAreaId(areaId);
		up.setIsPublic(false);

		mockMvc.perform(put("/api/properties/{id}", id)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(up))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update property successfully"))
			.andExpect(jsonPath("$.data.title").value("Prop B"))
			.andExpect(jsonPath("$.data.isPublic").value(false));

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
		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk());
		// list
		mockMvc.perform(get("/api/properties?page=0&size=5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meta.total").exists())
			.andExpect(jsonPath("$.data.content").isArray());
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
}


