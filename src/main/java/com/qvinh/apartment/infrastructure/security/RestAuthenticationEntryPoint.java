package com.qvinh.apartment.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvinh.apartment.infrastructure.web.RequestIdFilter;
import com.qvinh.apartment.shared.api.error.ApiError;
import com.qvinh.apartment.shared.api.error.ApiErrorResponse;
import com.qvinh.apartment.shared.constants.ErrorMessages;
import com.qvinh.apartment.shared.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
		String requestId = Objects.toString(request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR), "");
		if (requestId.isBlank()) {
			requestId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
		}
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setHeader(RequestIdFilter.REQUEST_ID_HEADER, requestId);

		ApiError error = new ApiError(ErrorCode.UNAUTHORIZED.name(), ErrorMessages.AUTHENTICATION_REQUIRED, requestId, null, null);
		objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.of(error));
	}
}
