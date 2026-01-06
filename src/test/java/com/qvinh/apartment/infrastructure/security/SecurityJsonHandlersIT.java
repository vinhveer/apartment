package com.qvinh.apartment.infrastructure.security;

import com.qvinh.apartment.contract.ContractAsserts;
import com.qvinh.apartment.testsupport.InfrastructureITBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class SecurityJsonHandlersIT extends InfrastructureITBase {

	@Test
	@DisplayName("401 returns JSON (not HTML) even when Accept=text/html")
	void unauthorized_is_json_even_when_accepts_html() throws Exception {
		var resp = mockMvc.perform(get("/api/users/me")
				.accept(MediaType.TEXT_HTML))
			.andReturn().getResponse();

		ContractAsserts.assertErrorResponse(resp, 401, "UNAUTHORIZED");
		assertThat(resp.getContentAsString().toLowerCase()).doesNotContain("<html");
	}

	@Test
	@DisplayName("403 returns JSON (not HTML) even when Accept=text/html")
	void forbidden_is_json_even_when_accepts_html() throws Exception {
		String access = login("user01", "123456");
		var resp = mockMvc.perform(get("/api/accounts/customers")
				.header(AUTH, BEARER + access)
				.accept(MediaType.TEXT_HTML))
			.andReturn().getResponse();

		ContractAsserts.assertErrorResponse(resp, 403, "FORBIDDEN");
		assertThat(resp.getContentAsString().toLowerCase()).doesNotContain("<html");
	}
}

