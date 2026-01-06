package com.qvinh.apartment.features.properties.dto.property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyGalleryRes {
	private Long fileId;
	private String originalName;
	private String relativePath;
	private String mimeType;
	private String altText;
	private String title;
}
