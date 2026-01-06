package com.qvinh.apartment.shared.exception;

import com.qvinh.apartment.shared.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {
	public ResourceNotFoundException(ErrorCode code, String message) {
		super(code, HttpStatus.NOT_FOUND, message);
	}

	public ResourceNotFoundException(String message) {
		this(ErrorCode.RESOURCE_NOT_FOUND, message);
	}
}
