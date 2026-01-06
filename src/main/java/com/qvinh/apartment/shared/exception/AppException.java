package com.qvinh.apartment.shared.exception;

import com.qvinh.apartment.shared.error.ErrorCode;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
	private final ErrorCode code;
	private final HttpStatus status;
	private final Map<String, Object> details;

	public AppException(ErrorCode code, HttpStatus status, String message) {
		this(code, status, message, null);
	}

	public AppException(ErrorCode code, HttpStatus status, String message, Map<String, Object> details) {
		super(message);
		this.code = code;
		this.status = status;
		this.details = details;
	}

	public ErrorCode getCode() { return code; }
	public HttpStatus getStatus() { return status; }
	public Map<String, Object> getDetails() { return details; }
}

