package com.qvinh.apartment.features.properties.dto.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertySaleInfoRes {
	private Long userId;
	private String displayName;
	private String phone;
}
