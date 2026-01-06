package com.qvinh.apartment.shared.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
	private String code;
	private String message;
	private String requestId;
	private List<FieldErrorItem> fieldErrors;
	private Map<String, Object> details;
}
