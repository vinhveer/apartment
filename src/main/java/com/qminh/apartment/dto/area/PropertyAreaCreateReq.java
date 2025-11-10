package com.qminh.apartment.dto.area;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PropertyAreaCreateReq {
	@NotBlank
	@Size(max = 255)
	private String areaName;
	@NotBlank
	@Size(max = 255)
	private String areaLink;

	public String getAreaName() { return areaName; }
	public void setAreaName(String areaName) { this.areaName = areaName; }
	public String getAreaLink() { return areaLink; }
	public void setAreaLink(String areaLink) { this.areaLink = areaLink; }
}


