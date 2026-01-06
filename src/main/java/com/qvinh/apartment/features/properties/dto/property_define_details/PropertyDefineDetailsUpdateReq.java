package com.qvinh.apartment.features.properties.dto.property_define_details;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

	public String getDetailName() { return detailName; }
	public void setDetailName(String detailName) { this.detailName = detailName; }
	public Boolean getIsNumber() { return isNumber; }
	public void setIsNumber(Boolean isNumber) { this.isNumber = isNumber; }
	public String getUnit() { return unit; }
	public void setUnit(String unit) { this.unit = unit; }
	public Boolean getShowInHomePage() { return showInHomePage; }
	public void setShowInHomePage(Boolean showInHomePage) { this.showInHomePage = showInHomePage; }
}

