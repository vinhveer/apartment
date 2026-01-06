package com.qvinh.apartment.features.files.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FileRenameReq {

	@NotBlank
	@Size(max = 255)
	private String originalName;

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}
}

