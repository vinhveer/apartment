package com.qvinh.apartment.features.properties.dto.property_type;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyTypeUpdateReq {
	@NotBlank
	@Size(max = 255)
	private String typeName;
}
