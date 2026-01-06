package com.qvinh.apartment.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.qvinh.apartment.contract.ContractAsserts;
import com.qvinh.apartment.testsupport.InfrastructureITBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequestIdFilterIT extends InfrastructureITBase {

	@Test
	@DisplayName("X-Request-Id is generated, set in header, and available in MDC during request")
	void request_id_generated_and_mdc_available() throws Exception {
		String access = login("root", "123456");
		var resp = mockMvc.perform(get("/__infra/mdc")
				.header(AUTH, BEARER + access))
			.andExpect(status().isOk())
			.andReturn().getResponse();

		String headerId = resp.getHeader("X-Request-Id");
		assertThat(headerId).isNotBlank();
		assertThat(resp.getContentAsString()).isEqualTo(headerId);
	}

	@Test
	@DisplayName("X-Request-Id is propagated from incoming header")
	void request_id_propagated() throws Exception {
		String access = login("root", "123456");
		var resp = mockMvc.perform(get("/__infra/mdc")
				.header(AUTH, BEARER + access)
				.header("X-Request-Id", "rid-infra-1"))
			.andExpect(status().isOk())
			.andReturn().getResponse();

		assertThat(resp.getHeader("X-Request-Id")).isEqualTo("rid-infra-1");
		assertThat(resp.getContentAsString()).isEqualTo("rid-infra-1");
	}

	@Test
	@DisplayName("MDC requestId does not leak across requests")
	void request_id_mdc_does_not_leak() throws Exception {
		String access = login("root", "123456");
		var resp1 = mockMvc.perform(get("/__infra/mdc")
				.header(AUTH, BEARER + access)
				.header("X-Request-Id", "rid-infra-leak"))
			.andExpect(status().isOk())
			.andReturn().getResponse();
		assertThat(resp1.getContentAsString()).isEqualTo("rid-infra-leak");

		var resp2 = mockMvc.perform(get("/__infra/mdc")
				.header(AUTH, BEARER + access))
			.andExpect(status().isOk())
			.andReturn().getResponse();
		assertThat(resp2.getContentAsString()).isNotEqualTo("rid-infra-leak");
	}

	@Test
	@DisplayName("Error response requestId matches header and respects incoming X-Request-Id")
	void error_response_request_id_matches() throws Exception {
		var resp = mockMvc.perform(get("/api/users/me")
				.header("X-Request-Id", "rid-error-1")
				.accept(MediaType.APPLICATION_JSON))
			.andReturn().getResponse();

		JsonNode root = ContractAsserts.assertErrorResponse(resp, 401, "UNAUTHORIZED");
		assertThat(root.path("error").path("requestId").asText()).isEqualTo("rid-error-1");
		assertThat(resp.getHeader("X-Request-Id")).isEqualTo("rid-error-1");
	}

	@Test
	@DisplayName("Success responses also include X-Request-Id header")
	void success_response_has_request_id_header() throws Exception {
		var resp = mockMvc.perform(post("/api/auth/login")
				.header("X-Request-Id", "rid-success-1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"root\",\"password\":\"123456\"}"))
			.andExpect(status().isOk())
			.andReturn().getResponse();

		assertThat(resp.getHeader("X-Request-Id")).isEqualTo("rid-success-1");
	}
}
