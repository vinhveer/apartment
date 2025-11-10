package com.qminh.apartment.exception;

import com.qminh.apartment.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ApiResponse.error("NOT_FOUND", ex.getMessage(), null));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleSql(DataIntegrityViolationException ex) {
		Throwable root = ex.getRootCause();
		String details = (root != null ? root.getMessage() : ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(ApiResponse.error("SQL_CONSTRAINT", "Constraint violation", details));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError e : ex.getBindingResult().getFieldErrors()) {
			errors.put(e.getField(), e.getDefaultMessage());
		}
		return ResponseEntity.badRequest()
			.body(ApiResponse.error("VALIDATION_ERROR", "Invalid request parameters", errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.error("INTERNAL_ERROR", "Internal server error", ex.getMessage()));
	}
}


