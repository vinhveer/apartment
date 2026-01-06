package com.qvinh.apartment.features.properties.dto.property;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertySelectRes {
	private Long propertyId;
	private String title;
	private BigDecimal price;
	private String description;
	private Boolean isPublic;
	private Boolean isForRent;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private PropertyTypeInfo type;
	private PropertyAreaInfo area;
	private PropertySaleInfoRes saleInfo;
	private List<PropertyGalleryRes> galleries;
	private List<PropertyDetailRes> details;
}
