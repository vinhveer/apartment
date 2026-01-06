package com.qvinh.apartment.features.properties.dto.property_type;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PropertyTypeCreateReq {
	@NotBlank
	@Size(max = 255)
	private String typeName;

	public String getTypeName() { return typeName; }
	public void setTypeName(String typeName) { this.typeName = typeName; }
}

