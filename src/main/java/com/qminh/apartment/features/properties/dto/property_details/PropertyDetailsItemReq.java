package com.qminh.apartment.features.properties.dto.property_details;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PropertyDetailsItemReq {

	@NotNull(message = "detailId is required")
	private Integer detailId;

	@Size(max = 65535, message = "value is too long")
	private String value;

	public Integer getDetailId() {
		return detailId;
	}

	public void setDetailId(Integer detailId) {
		this.detailId = detailId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

