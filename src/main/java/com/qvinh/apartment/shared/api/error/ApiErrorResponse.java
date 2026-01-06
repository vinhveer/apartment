package com.qvinh.apartment.shared.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
public class ApiErrorResponse {
	private boolean success = false;
	@Setter
	private ApiError error;

	public ApiErrorResponse(ApiError error) {
		this.error = error;
	}

	public static ApiErrorResponse of(ApiError error) {
		return new ApiErrorResponse(error);
	}
}
