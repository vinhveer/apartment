package com.qvinh.apartment.features.properties.dto.property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyDetailRes {
	private Integer detailId;
	private String detailName;
	private Boolean isNumber;
	private String unit;
	private Boolean showInHomePage;
	private String value;
}
