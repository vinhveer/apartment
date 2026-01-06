package com.qvinh.apartment.shared.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
	private boolean success = false;
	private ApiError error;

	public ApiErrorResponse() {}

	public ApiErrorResponse(ApiError error) {
		this.error = error;
	}

	public static ApiErrorResponse of(ApiError error) {
		return new ApiErrorResponse(error);
	}

	public boolean isSuccess() { return success; }
	public ApiError getError() { return error; }
	public void setError(ApiError error) { this.error = error; }
}

