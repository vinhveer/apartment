package com.qvinh.apartment.features.properties.dto.property;

public class PropertyAreaInfo {
	private Integer areaId;
	private String areaName;
	private String areaLink;

	public PropertyAreaInfo() {}

	public PropertyAreaInfo(Integer areaId, String areaName, String areaLink) {
		this.areaId = areaId;
		this.areaName = areaName;
		this.areaLink = areaLink;
	}

	public Integer getAreaId() { return areaId; }
	public void setAreaId(Integer areaId) { this.areaId = areaId; }
	public String getAreaName() { return areaName; }
	public void setAreaName(String areaName) { this.areaName = areaName; }
	public String getAreaLink() { return areaLink; }
	public void setAreaLink(String areaLink) { this.areaLink = areaLink; }
}
