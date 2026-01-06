package com.qvinh.apartment.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class ValidationContractIT extends ContractTestBase {

	@Test
	@DisplayName("422 validation error contains fieldErrors")
	void validation_422_has_field_errors() throws Exception {
		String access = login("root", "123456");
		var resp = mockMvc.perform(post("/api/create-employee-account")
				.header(AUTH, BEARER + access)
				.header("X-Request-Id", "rid-validation-contract")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{}"))
			.andReturn().getResponse();
		ContractAsserts.assertValidationError(resp);
	}

	@Test
	@DisplayName("400 malformed JSON returns MALFORMED_JSON in JSON envelope")
	void malformed_json_400() throws Exception {
		String access = login("root", "123456");
		var resp = mockMvc.perform(post("/api/create-employee-account")
				.header(AUTH, BEARER + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{"))
			.andReturn().getResponse();
		ContractAsserts.assertErrorResponse(resp, 400, "MALFORMED_JSON");
	}
}
