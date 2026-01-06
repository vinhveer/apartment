package com.qvinh.apartment.infrastructure.web;

import com.qvinh.apartment.shared.api.error.ApiError;
import com.qvinh.apartment.shared.api.error.ApiErrorResponse;
import com.qvinh.apartment.shared.api.error.FieldErrorItem;
import com.qvinh.apartment.shared.exception.BusinessException;
import com.qvinh.apartment.shared.exception.ConflictException;
import com.qvinh.apartment.shared.exception.AppException;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.shared.error.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String REQUEST_ID_ATTR = "requestId";
	private static final String REQUEST_ID_HEADER = "X-Request-Id";

	private String requestId(HttpServletRequest request) {
		Object v = request.getAttribute(REQUEST_ID_ATTR);
		if (v instanceof String s && !s.isBlank()) return s;
		String header = request.getHeader(REQUEST_ID_HEADER);
		return (header == null || header.isBlank()) ? "unknown" : header;
	}

	private ResponseEntity<ApiErrorResponse> build(HttpStatus status, ErrorCode code, String message, String requestId,
	                                              List<FieldErrorItem> fieldErrors, Map<String, Object> details) {
		if (code == ErrorCode.VALIDATION_ERROR && (fieldErrors == null || fieldErrors.isEmpty())) {
			fieldErrors = List.of(new FieldErrorItem("request", message));
		}
		ApiError error = new ApiError(code.name(), message, requestId, fieldErrors, details);
		return ResponseEntity.status(status)
			.header(REQUEST_ID_HEADER, requestId)
			.body(ApiErrorResponse.of(error));
	}

	@ExceptionHandler(AppException.class)
	public ResponseEntity<ApiErrorResponse> handleApp(AppException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		return build(ex.getStatus(), ex.getCode(), ex.getMessage(), requestId, null, ex.getDetails());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), requestId, null, null);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		return build(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), requestId, null, null);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleSql(DataIntegrityViolationException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		return build(HttpStatus.CONFLICT, ErrorCode.RESOURCE_CONFLICT, "Conflict", requestId, null, null);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCode(), ex.getMessage(), requestId, null, null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		List<FieldErrorItem> errors = ex.getBindingResult().getFieldErrors().stream()
			.map(e -> new FieldErrorItem(e.getField(), Objects.toString(e.getDefaultMessage(), "Invalid value")))
			.collect(Collectors.toList());
		return build(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.VALIDATION_ERROR, "Validation failed", requestId, errors, null);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		List<FieldErrorItem> errors = ex.getConstraintViolations().stream()
			.map(v -> new FieldErrorItem(v.getPropertyPath().toString(), v.getMessage()))
			.collect(Collectors.toList());
		return build(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.VALIDATION_ERROR, "Validation failed", requestId, errors, null);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		List<FieldErrorItem> errors = List.of(new FieldErrorItem(ex.getParameterName(), "Missing required parameter"));
		return build(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.VALIDATION_ERROR, "Validation failed", requestId, errors, null);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		return build(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_JSON, "Malformed JSON", requestId, null, null);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		return build(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.VALIDATION_ERROR, "Validation failed", requestId,
			List.of(new FieldErrorItem("request", ex.getMessage())), null);
	}

	@ExceptionHandler({ AccessDeniedException.class, AuthorizationDeniedException.class })
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(Exception ex, HttpServletRequest request) {
		String requestId = requestId(request);
		return build(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "Access denied", requestId, null, null);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest request) {
		String requestId = requestId(request);
		return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Authentication required", requestId, null, null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleOther(Exception ex, HttpServletRequest request) {
		String requestId = requestId(request);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Internal server error", requestId, null, null);
	}
}
