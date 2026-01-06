package com.qvinh.apartment.features.properties.dto.property_gallery;

import jakarta.validation.constraints.NotNull;

public class PropertyGalleryCreateReq {

	@NotNull
	private Long fileId;

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}
}


