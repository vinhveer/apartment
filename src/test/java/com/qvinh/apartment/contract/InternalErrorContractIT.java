package com.qvinh.apartment.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class InternalErrorContractIT extends ContractTestBase {

	@Test
	@DisplayName("500 fallback internal error returns INTERNAL_ERROR")
	void internal_500() throws Exception {
		String access = login("root", "123456");
		var resp = mockMvc.perform(get("/__contract/boom")
				.header(AUTH, BEARER + access))
			.andReturn().getResponse();
		ContractAsserts.assertErrorResponse(resp, 500, "INTERNAL_ERROR");
	}
}
