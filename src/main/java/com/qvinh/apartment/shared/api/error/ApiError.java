package com.qvinh.apartment.shared.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
	private String code;
	private String message;
	private String requestId;
	private List<FieldErrorItem> fieldErrors;
	private Map<String, Object> details;

	public ApiError() {}

	public ApiError(String code, String message, String requestId, List<FieldErrorItem> fieldErrors, Map<String, Object> details) {
		this.code = code;
		this.message = message;
		this.requestId = requestId;
		this.fieldErrors = fieldErrors;
		this.details = details;
	}

	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	public String getRequestId() { return requestId; }
	public void setRequestId(String requestId) { this.requestId = requestId; }
	public List<FieldErrorItem> getFieldErrors() { return fieldErrors; }
	public void setFieldErrors(List<FieldErrorItem> fieldErrors) { this.fieldErrors = fieldErrors; }
	public Map<String, Object> getDetails() { return details; }
	public void setDetails(Map<String, Object> details) { this.details = details; }
}

