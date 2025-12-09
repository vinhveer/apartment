package com.qminh.apartment.dto.property;

public class PropertyDetailRes {
	private Integer detailId;
	private String detailName;
	private Boolean isNumber;
	private String unit;
	private Boolean showInHomePage;
	private String value;

	public PropertyDetailRes() {}

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
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
}
