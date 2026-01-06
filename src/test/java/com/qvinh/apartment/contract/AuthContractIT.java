package com.qvinh.apartment.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class AuthContractIT extends ContractTestBase {

	@Test
	@DisplayName("401 unauthorized (no token) returns JSON error envelope")
	void unauthorized_no_token() throws Exception {
		var resp = mockMvc.perform(get("/api/users/me"))
			.andReturn().getResponse();
		ContractAsserts.assertErrorResponse(resp, 401, "UNAUTHORIZED");
	}

	@Test
	@DisplayName("401 unauthorized (invalid token) returns JSON error envelope")
	void unauthorized_invalid_token() throws Exception {
		var resp = mockMvc.perform(get("/api/users/me")
				.header(AUTH, BEARER + "invalid_token"))
			.andReturn().getResponse();
		ContractAsserts.assertErrorResponse(resp, 401, "UNAUTHORIZED");
	}

	@Test
	@DisplayName("403 forbidden (insufficient role) returns JSON error envelope")
	void forbidden_insufficient_role() throws Exception {
		String access = login("user01", "123456");
		var resp = mockMvc.perform(get("/api/accounts/customers")
				.header(AUTH, BEARER + access)
				.accept(MediaType.APPLICATION_JSON))
			.andReturn().getResponse();
		ContractAsserts.assertErrorResponse(resp, 403, "FORBIDDEN");
	}
}
