package com.qvinh.apartment.shared.exception;

import com.qvinh.apartment.shared.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class BusinessException extends AppException {
	public BusinessException(String message) {
		super(ErrorCode.BUSINESS_ERROR, HttpStatus.UNPROCESSABLE_ENTITY, message);
	}
}
