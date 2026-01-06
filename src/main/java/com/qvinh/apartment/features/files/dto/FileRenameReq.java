package com.qvinh.apartment.features.files.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileRenameReq {

	@NotBlank
	@Size(max = 255)
	private String originalName;
}
