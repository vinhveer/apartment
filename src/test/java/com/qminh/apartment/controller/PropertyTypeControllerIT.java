package com.qminh.apartment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.dto.property_type.PropertyTypeCreateReq;
import com.qminh.apartment.dto.property_type.PropertyTypeUpdateReq;
import com.qminh.apartment.testsupport.PostgresTestContainer;
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
@AutoConfigureMockMvc
class PropertyTypeControllerIT extends PostgresTestContainer {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper mapper;

	@Test
	@DisplayName("POST/GET/PUT/DELETE /api/property-types works and returns ApiResponse")
	void crud_flow() throws Exception {
		PropertyTypeCreateReq req = new PropertyTypeCreateReq();
		req.setTypeName("Apartment");

		String createRes = mockMvc.perform(post("/api/property-types")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Create property type successfully"))
			.andExpect(jsonPath("$.data.typeId").exists())
			.andReturn().getResponse().getContentAsString();

		int id = mapper.readTree(createRes).path("data").path("typeId").asInt();

		mockMvc.perform(get("/api/property-types/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property type detail"))
			.andExpect(jsonPath("$.data.typeName").value("Apartment"));

		PropertyTypeUpdateReq up = new PropertyTypeUpdateReq();
		up.setTypeName("Condo");

		mockMvc.perform(put("/api/property-types/{id}", id)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(up))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update property type successfully"))
			.andExpect(jsonPath("$.data.typeName").value("Condo"));

		mockMvc.perform(delete("/api/property-types/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete property type successfully"));
	}
}


