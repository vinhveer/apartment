package com.qvinh.apartment.features.properties.dto.property_details;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyDetailsItemReq {

	@NotNull(message = "detailId is required")
	private Integer detailId;

	@Size(max = 65535, message = "value is too long")
	private String value;
}
