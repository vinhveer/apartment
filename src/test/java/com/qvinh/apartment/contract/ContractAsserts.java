package com.qvinh.apartment.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

public final class ContractAsserts {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ContractAsserts() {}

	public static JsonNode assertErrorResponse(MockHttpServletResponse resp, int expectedStatus, String expectedCode) throws Exception {
		assertThat(resp.getStatus()).isEqualTo(expectedStatus);
		assertThat(resp.getContentType()).isNotNull();
		assertThat(MediaType.parseMediaType(Objects.requireNonNull(resp.getContentType())))
			.satisfies(mt -> assertThat(mt.isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue());

		String headerRequestId = resp.getHeader("X-Request-Id");
		assertThat(headerRequestId).isNotBlank();

		JsonNode root = MAPPER.readTree(resp.getContentAsString());
		assertThat(root.path("success").asBoolean()).isFalse();
		assertThat(root.hasNonNull("error")).isTrue();
		assertThat(root.path("error").path("code").asText()).isEqualTo(expectedCode);
		assertThat(root.path("error").path("message").asText()).isNotBlank();
		assertThat(root.path("error").path("requestId").asText()).isEqualTo(headerRequestId);
		return root;
	}

	public static void assertValidationError(MockHttpServletResponse resp) throws Exception {
		JsonNode root = assertErrorResponse(resp, 422, "VALIDATION_ERROR");
		assertThat(root.path("error").path("fieldErrors").isArray()).isTrue();
		assertThat(root.path("error").path("fieldErrors").size()).isGreaterThan(0);
		assertThat(root.path("error").path("fieldErrors").get(0).path("field").asText()).isNotBlank();
		assertThat(root.path("error").path("fieldErrors").get(0).path("message").asText()).isNotBlank();
	}
}

