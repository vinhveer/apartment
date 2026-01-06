package com.qvinh.apartment.features.properties.dto.property_details;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyDetailsUpdateReq {

	@NotEmpty(message = "items must not be empty")
	@Valid
	private List<PropertyDetailsItemReq> items;
}
