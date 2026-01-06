package com.qminh.apartment.features.properties.dto.property_details;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class PropertyDetailsCreateReq {

	@NotEmpty(message = "items must not be empty")
	@Valid
	private List<PropertyDetailsItemReq> items;

	public List<PropertyDetailsItemReq> getItems() {
		return items;
	}

	public void setItems(List<PropertyDetailsItemReq> items) {
		this.items = items;
	}
}

