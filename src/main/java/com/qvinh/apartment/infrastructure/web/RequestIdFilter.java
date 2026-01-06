package com.qvinh.apartment.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

	public static final String REQUEST_ID_HEADER = "X-Request-Id";
	public static final String REQUEST_ID_ATTR = "requestId";
	public static final String MDC_KEY = "requestId";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String incoming = request.getHeader(REQUEST_ID_HEADER);
		String requestId = (incoming == null || incoming.isBlank()) ? UUID.randomUUID().toString() : incoming;

		request.setAttribute(REQUEST_ID_ATTR, requestId);
		MDC.put(MDC_KEY, requestId);
		response.setHeader(REQUEST_ID_HEADER, requestId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}
}

