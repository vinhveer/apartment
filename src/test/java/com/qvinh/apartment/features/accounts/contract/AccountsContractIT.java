package com.qvinh.apartment.features.accounts.contract;

import com.qvinh.apartment.contract.ContractAsserts;
import com.qvinh.apartment.contract.ContractTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountsContractIT extends ContractTestBase {

	@Test
	@DisplayName("404 USER_NOT_FOUND on employee update")
	void not_found_404_user() throws Exception {
		String access = login("root", "123456");
		var resp = mockMvc.perform(put("/api/accounts/employees/999999")
				.header(AUTH, BEARER + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{\"displayName\":\"x\"}"))
			.andReturn().getResponse();
		ContractAsserts.assertErrorResponse(resp, 404, "USER_NOT_FOUND");
	}

	@Test
	@DisplayName("409 USERNAME_ALREADY_EXISTS on duplicate employee create")
	void conflict_409_duplicate_username() throws Exception {
		String access = login("root", "123456");
		String unique = "dup" + System.nanoTime();
		String body = """
			{
			  "username": "%s",
			  "email": "%s@example.com",
			  "password": "123456",
			  "displayName": "Dup",
			  "fullName": "Dup Full",
			  "phone": "0900",
			  "roleName": "SALE"
			}
			""".formatted(unique, unique);

		mockMvc.perform(post("/api/create-employee-account")
				.header(AUTH, BEARER + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andExpect(status().isOk());

		var resp = mockMvc.perform(post("/api/create-employee-account")
				.header(AUTH, BEARER + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andReturn().getResponse();

		ContractAsserts.assertErrorResponse(resp, 409, "USERNAME_ALREADY_EXISTS");
	}
}

