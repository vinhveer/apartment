package com.qvinh.apartment.features.properties.dto.property_gallery;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyGalleryRes {

	private Long propertyId;
	private Long fileId;
	private String filePath;
}

