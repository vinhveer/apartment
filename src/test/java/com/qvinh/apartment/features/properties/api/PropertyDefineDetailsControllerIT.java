package com.qvinh.apartment.features.properties.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsCreateReq;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsUpdateReq;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PropertyDefineDetailsControllerIT extends PostgresTestContainer {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper mapper;

	@Test
	@DisplayName("POST/GET/PUT/DELETE /api/property-define-details works and returns ApiResponse")
	void crud_flow() throws Exception {
		PropertyDefineDetailsCreateReq req = new PropertyDefineDetailsCreateReq();
		req.setDetailName("bedrooms");
		req.setIsNumber(true);
		req.setUnit("room");
		req.setShowInHomePage(true);

		String createRes = mockMvc.perform(post("/api/property-define-details")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Create property define details successfully"))
			.andExpect(jsonPath("$.data.detailId").exists())
			.andReturn().getResponse().getContentAsString();

		int id = mapper.readTree(createRes).path("data").path("detailId").asInt();

		mockMvc.perform(get("/api/property-define-details/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property define details detail"))
			.andExpect(jsonPath("$.data.detailName").value("bedrooms"))
			.andExpect(jsonPath("$.data.isNumber").value(true))
			.andExpect(jsonPath("$.data.unit").value("room"))
			.andExpect(jsonPath("$.data.showInHomePage").value(true));

		PropertyDefineDetailsUpdateReq up = new PropertyDefineDetailsUpdateReq();
		up.setDetailName("bathrooms");
		up.setIsNumber(true);
		up.setUnit("room");
		up.setShowInHomePage(false);

		mockMvc.perform(put("/api/property-define-details/{id}", id)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(up))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update property define details successfully"))
			.andExpect(jsonPath("$.data.detailName").value("bathrooms"))
			.andExpect(jsonPath("$.data.showInHomePage").value(false));

		mockMvc.perform(delete("/api/property-define-details/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete property define details successfully"));
	}

	@Test
	@DisplayName("GET list returns meta and data; 404/409/400 cases")
	void list_and_error_cases() throws Exception {
		// create one
		PropertyDefineDetailsCreateReq req = new PropertyDefineDetailsCreateReq();
		req.setDetailName("defA");
		req.setIsNumber(false);
		req.setShowInHomePage(false);
		mockMvc.perform(post("/api/property-define-details")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk());
		// list
		mockMvc.perform(get("/api/property-define-details?page=0&size=5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meta.total").exists())
			.andExpect(jsonPath("$.data.content").isArray());
		// 404 get/update/delete not found
		mockMvc.perform(get("/api/property-define-details/{id}", 999999))
			.andExpect(status().isNotFound());
		PropertyDefineDetailsUpdateReq up = new PropertyDefineDetailsUpdateReq();
		up.setDetailName("X");
		up.setIsNumber(false);
		up.setShowInHomePage(false);
		mockMvc.perform(put("/api/property-define-details/{id}", 999999)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(up))))
			.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/property-define-details/{id}", 999999))
			.andExpect(status().isNotFound());
		// 409 duplicate
		PropertyDefineDetailsCreateReq dup = new PropertyDefineDetailsCreateReq();
		dup.setDetailName("defA");
		dup.setIsNumber(false);
		dup.setShowInHomePage(false);
		mockMvc.perform(post("/api/property-define-details")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(dup))))
			.andExpect(status().isConflict());
			// 422 validation missing
			mockMvc.perform(post("/api/property-define-details")
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content("{}"))
				.andExpect(status().isUnprocessableEntity());
		}
	}
