package com.qvinh.apartment.shared.exception;

import com.qvinh.apartment.shared.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {
	public ConflictException(ErrorCode code, String message) {
		super(code, HttpStatus.CONFLICT, message);
	}

	public ConflictException(String message) {
		this(ErrorCode.RESOURCE_CONFLICT, message);
	}
}

