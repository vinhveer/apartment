package com.qvinh.apartment.features.properties.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvinh.apartment.features.accounts.application.AccountService;
import com.qvinh.apartment.features.accounts.domain.*;
import com.qvinh.apartment.features.accounts.dto.account.AccountCreateReq;
import com.qvinh.apartment.features.accounts.persistence.*;
import com.qvinh.apartment.features.auth.dto.LoginReq;
import com.qvinh.apartment.features.properties.domain.*;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsCreateReq;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsItemReq;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsUpdateReq;
import com.qvinh.apartment.features.properties.persistence.*;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PropertyDetailsControllerIT extends PostgresTestContainer {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper mapper;

	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private PropertySaleInfoRepository saleInfoRepository;
	@Autowired private PropertyAreaRepository areaRepository;
	@Autowired private PropertyTypeRepository typeRepository;
	@Autowired private PropertyRepository propertyRepository;
	@Autowired private PropertyDefineDetailsRepository defineRepository;
	@Autowired private AccountService accountService;

	private static final String AUTH = "Authorization";
	private static final String BEARER = "Bearer ";
	private Long propertyId;
	private Integer dSizeId;
	private Integer dInteriorId;

	@BeforeEach
	void setup() {
		// Roles
		if (roleRepository.findByRoleName("USER").isEmpty()) {
			Role r = new Role(); r.setRoleName("USER"); roleRepository.saveAndFlush(r);
		}
		// User
		if (userRepository.findByUsername("pdtester").isEmpty()) {
			Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
			User u = new User();
			u.setUsername("pdtester");
			u.setEmail("pdtester@example.com");
			u.setPassword(passwordEncoder.encode("123456"));
			u.setRole(userRole);
			userRepository.saveAndFlush(u);
		}
		// Ensure SALE role exists
		if (roleRepository.findByRoleName("SALE").isEmpty()) {
			Role r = new Role(); r.setRoleName("SALE"); roleRepository.saveAndFlush(r);
		}
		// Create sale account via service (transactional: user + sale info)
		String saleUsername = "sale_user_" + System.nanoTime();
		String saleEmail = "sale_user_" + System.nanoTime() + "@example.com";
		AccountCreateReq createSale = new AccountCreateReq();
		createSale.setUsername(saleUsername);
		createSale.setEmail(saleEmail);
		createSale.setPassword("123456");
		createSale.setDisplayName("Sale Disp");
		createSale.setFullName("PD Tester");
		createSale.setPhone("0900000000");
		createSale.setRoleName("SALE");
		accountService.createEmployeeAccount(createSale);
		User saleUser = userRepository.findByUsername(saleUsername).orElseThrow();
		PropertySaleInfo info = saleInfoRepository.findByUserId(Objects.requireNonNull(saleUser.getId())).orElseThrow();
		// Area
		PropertyArea area = areaRepository.findAll().stream().findFirst().orElseGet(() -> {
			PropertyArea a = new PropertyArea();
			a.setAreaName("Hanoi");
			a.setAreaLink("hanoi-" + System.nanoTime());
			return areaRepository.saveAndFlush(a);
		});
		// Type
		PropertyType type = typeRepository.findAll().stream().findFirst().orElseGet(() -> {
			PropertyType t = new PropertyType();
			t.setTypeName("Apartment-" + System.nanoTime());
			return typeRepository.saveAndFlush(t);
		});
		// Property
		Property p = new Property();
		p.setTitle("Nice apt");
		p.setPrice(new BigDecimal("123456.78"));
		p.setDescription("Desc");
		p.setType(type);
		p.setSaleInfo(info);
		p.setArea(area);
		propertyId = propertyRepository.saveAndFlush(p).getPropertyId();

		// Define dedicated details for this test (avoid relying on global state)
		PropertyDefineDetails d1 = new PropertyDefineDetails();
		d1.setDetailName("Size-" + System.nanoTime());
		d1.setIsNumber(Boolean.TRUE);
		d1.setUnit("m2");
		d1.setShowInHomePage(Boolean.TRUE);
		dSizeId = defineRepository.saveAndFlush(d1).getDetailId();

		PropertyDefineDetails d2 = new PropertyDefineDetails();
		d2.setDetailName("Interior-" + System.nanoTime());
		d2.setIsNumber(Boolean.FALSE);
		d2.setUnit(null);
		d2.setShowInHomePage(Boolean.FALSE);
		dInteriorId = defineRepository.saveAndFlush(d2).getDetailId();
	}

	private String login() throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername("pdtester");
		req.setPassword("123456");
		var res = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return mapper.readTree(res).path("data").path("accessToken").asText();
	}

	@Test
	@DisplayName("POST/PATCH/GET/DELETE PropertyDetails works and returns ApiResponse")
	void property_details_crud_flow() throws Exception {
		String access = login();

		// Create multiple details
		var cReq = new PropertyDetailsCreateReq();
		var it1 = new PropertyDetailsItemReq(); it1.setDetailId(dSizeId); it1.setValue("120");
		var it2 = new PropertyDetailsItemReq(); it2.setDetailId(dInteriorId); it2.setValue("Đầy đủ nội thất");
		cReq.setItems(List.of(it1, it2));

		mockMvc.perform(post("/api/properties/{pid}/details", propertyId)
				.header(AUTH, BEARER + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(cReq))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Create property details successfully"))
			.andExpect(jsonPath("$.data[0].propertyId").value(propertyId))
			.andExpect(jsonPath("$.data[1].propertyId").value(propertyId));

		// Read back
		mockMvc.perform(get("/api/properties/{pid}/details", propertyId)
				.header(AUTH, BEARER + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property details"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].detailId").value(dSizeId))
			.andExpect(jsonPath("$.data[1].detailId").value(dInteriorId));

		// Update values
		var uReq = new PropertyDetailsUpdateReq();
		var u1 = new PropertyDetailsItemReq(); u1.setDetailId(dSizeId); u1.setValue("125");
		var u2 = new PropertyDetailsItemReq(); u2.setDetailId(dInteriorId); u2.setValue("Nội thất cơ bản");
		uReq.setItems(List.of(u1, u2));

		mockMvc.perform(patch("/api/properties/{pid}/details", propertyId)
				.header(AUTH, BEARER + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(uReq))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update property details successfully"))
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].detailId").value(dSizeId))
			.andExpect(jsonPath("$.data[0].value").value("125"))
			.andExpect(jsonPath("$.data[1].detailId").value(dInteriorId))
			.andExpect(jsonPath("$.data[1].value").value("Nội thất cơ bản"));

		// Delete one
		mockMvc.perform(delete("/api/properties/{pid}/details/{did}", propertyId, dSizeId)
				.header(AUTH, BEARER + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete property detail successfully"));

		mockMvc.perform(get("/api/properties/{pid}/details", propertyId)
				.header(AUTH, BEARER + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(1));

		// Delete all
		mockMvc.perform(delete("/api/properties/{pid}/details", propertyId)
				.header(AUTH, BEARER + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete all property details successfully"));

		mockMvc.perform(get("/api/properties/{pid}/details", propertyId)
				.header(AUTH, BEARER + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	@DisplayName("POST create same detail twice -> 409 CONFLICT")
	void create_conflict() throws Exception {
		String access = login();

		var cReq = new PropertyDetailsCreateReq();
		var it1 = new PropertyDetailsItemReq(); it1.setDetailId(dSizeId); it1.setValue("100");
		cReq.setItems(List.of(it1));

		mockMvc.perform(post("/api/properties/{pid}/details", propertyId)
				.header(AUTH, BEARER + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(cReq))))
			.andExpect(status().isOk());

			mockMvc.perform(post("/api/properties/{pid}/details", propertyId)
					.header(AUTH, BEARER + access)
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(Objects.requireNonNull(mapper.writeValueAsString(cReq))))
					.andExpect(status().isConflict())
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(false))
					.andExpect(jsonPath("$.error.code").value("PROPERTY_DETAILS_CONFLICT"))
					.andExpect(jsonPath("$.error.requestId").isNotEmpty());
			}

	@Test
	@DisplayName("POST numeric detail with non-numeric value -> 422 BUSINESS_ERROR")
	void create_numeric_invalid_value_422() throws Exception {
		String access = login();

		var cReq = new PropertyDetailsCreateReq();
		var it1 = new PropertyDetailsItemReq(); it1.setDetailId(dSizeId); it1.setValue("abc");
		cReq.setItems(List.of(it1));

			mockMvc.perform(post("/api/properties/{pid}/details", propertyId)
					.header(AUTH, BEARER + access)
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(Objects.requireNonNull(mapper.writeValueAsString(cReq))))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"))
				.andExpect(jsonPath("$.error.requestId").isNotEmpty());
		}

	@Test
	@DisplayName("PATCH numeric detail with non-numeric value -> 422 BUSINESS_ERROR")
	void update_numeric_invalid_value_422() throws Exception {
		String access = login();

		// Create a valid numeric detail first
		var cReq = new PropertyDetailsCreateReq();
		var it1 = new PropertyDetailsItemReq(); it1.setDetailId(dSizeId); it1.setValue("100");
		cReq.setItems(List.of(it1));
		mockMvc.perform(post("/api/properties/{pid}/details", propertyId)
				.header(AUTH, BEARER + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(cReq))))
			.andExpect(status().isOk());

		// Try to update with invalid number
		var uReq = new PropertyDetailsUpdateReq();
		var u1 = new PropertyDetailsItemReq(); u1.setDetailId(dSizeId); u1.setValue("xyz");
		uReq.setItems(List.of(u1));

			mockMvc.perform(patch("/api/properties/{pid}/details", propertyId)
					.header(AUTH, BEARER + access)
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(Objects.requireNonNull(mapper.writeValueAsString(uReq))))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"))
				.andExpect(jsonPath("$.error.requestId").isNotEmpty());
		}
	}
