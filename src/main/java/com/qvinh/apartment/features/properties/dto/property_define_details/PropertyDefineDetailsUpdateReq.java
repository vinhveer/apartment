package com.qvinh.apartment.features.properties.dto.property_define_details;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyDefineDetailsUpdateReq {
	@NotBlank
	@Size(max = 255)
	private String detailName;

	@NotNull
	private Boolean isNumber;

	@Size(max = 50)
	private String unit;

	@NotNull
	private Boolean showInHomePage;
}
