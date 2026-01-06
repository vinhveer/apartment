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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class RestAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public RestAccessDeniedHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
		String requestId = Objects.toString(request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR), "");
		if (requestId.isBlank()) {
			requestId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
		}
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setHeader(RequestIdFilter.REQUEST_ID_HEADER, requestId);

		ApiError error = new ApiError(ErrorCode.FORBIDDEN.name(), ErrorMessages.ACCESS_DENIED, requestId, null, null);
		objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.of(error));
	}
}
