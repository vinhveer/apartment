package com.qminh.apartment.features.properties.dto.property_define_details;

public class PropertyDefineDetailsRes {
	private Integer detailId;
	private String detailName;
	private Boolean isNumber;
	private String unit;
	private Boolean showInHomePage;

	public Integer getDetailId() { return detailId; }
	public void setDetailId(Integer detailId) { this.detailId = detailId; }
	public String getDetailName() { return detailName; }
	public void setDetailName(String detailName) { this.detailName = detailName; }
	public Boolean getIsNumber() { return isNumber; }
	public void setIsNumber(Boolean isNumber) { this.isNumber = isNumber; }
	public String getUnit() { return unit; }
	public void setUnit(String unit) { this.unit = unit; }
	public Boolean getShowInHomePage() { return showInHomePage; }
	public void setShowInHomePage(Boolean showInHomePage) { this.showInHomePage = showInHomePage; }
}

