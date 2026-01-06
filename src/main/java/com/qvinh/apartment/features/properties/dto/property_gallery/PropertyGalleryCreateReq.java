package com.qvinh.apartment.features.properties.dto.property_gallery;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyGalleryCreateReq {

	@NotNull
	private Long fileId;
}

