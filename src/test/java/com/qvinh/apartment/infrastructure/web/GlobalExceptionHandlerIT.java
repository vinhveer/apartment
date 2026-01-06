package com.qvinh.apartment.infrastructure.web;

import com.qvinh.apartment.contract.ContractAsserts;
import com.qvinh.apartment.testsupport.InfrastructureITBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class GlobalExceptionHandlerIT extends InfrastructureITBase {

	@Test
	@DisplayName("Missing request param maps to 422 VALIDATION_ERROR with fieldErrors")
	void missing_param_is_validation_error() throws Exception {
		String access = login("root", "123456");
		var resp = mockMvc.perform(get("/__infra/validate/missing")
				.header(AUTH, BEARER + access)
				.accept(MediaType.APPLICATION_JSON))
			.andReturn().getResponse();
		ContractAsserts.assertValidationError(resp);
	}

	@Test
	@DisplayName("Constraint violations map to 422 VALIDATION_ERROR with fieldErrors")
	void constraint_violation_is_validation_error() throws Exception {
		String access = login("root", "123456");
		var resp = mockMvc.perform(get("/__infra/validate/constraint")
				.header(AUTH, BEARER + access)
				.param("n", "0")
				.accept(MediaType.APPLICATION_JSON))
			.andReturn().getResponse();
		ContractAsserts.assertValidationError(resp);
	}

	@Test
	@DisplayName("Malformed JSON maps to 400 MALFORMED_JSON")
	void malformed_json_is_bad_request() throws Exception {
		var resp = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{"))
			.andReturn().getResponse();
		ContractAsserts.assertErrorResponse(resp, 400, "MALFORMED_JSON");
	}
}
