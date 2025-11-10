package com.qminh.apartment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.dto.area.PropertyAreaCreateReq;
import com.qminh.apartment.dto.area.PropertyAreaUpdateReq;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Objects;

@SpringBootTest
@AutoConfigureMockMvc
class PropertyAreaControllerIT extends PostgresTestContainer {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper mapper;

	@Test
	@DisplayName("POST/GET/PUT/DELETE /api/areas works and returns ApiResponse")
	void crud_flow() throws Exception {
		PropertyAreaCreateReq req = new PropertyAreaCreateReq();
		req.setAreaName("c1");
		req.setAreaLink("l1");

		String createRes = mockMvc.perform(post("/api/areas")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Create property area successfully"))
			.andExpect(jsonPath("$.data.areaId").exists())
			.andReturn().getResponse().getContentAsString();

		int id = mapper.readTree(createRes).path("data").path("areaId").asInt();

		mockMvc.perform(get("/api/areas/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property area detail"))
			.andExpect(jsonPath("$.data.areaName").value("c1"));

		PropertyAreaUpdateReq up = new PropertyAreaUpdateReq();
		up.setAreaName("c2");
		up.setAreaLink("l2");

		mockMvc.perform(put("/api/areas/{id}", id)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(up))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update property area successfully"))
			.andExpect(jsonPath("$.data.areaName").value("c2"));

		mockMvc.perform(delete("/api/areas/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete property area successfully"));
	}
}


