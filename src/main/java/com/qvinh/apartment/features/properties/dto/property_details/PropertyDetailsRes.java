package com.qvinh.apartment.features.properties.dto.property_details;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyDetailsRes {

	private Long propertyId;
	private Integer detailId;
	private String detailName;
	private Boolean isNumber;
	private String unit;
	private Boolean showInHomePage;
	private String value;
}
