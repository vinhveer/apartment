package com.qminh.apartment.dto.property;

public class PropertySaleInfoRes {
	private Long userId;
	private String displayName;
	private String phone;

	public PropertySaleInfoRes() {}

	public PropertySaleInfoRes(Long userId, String displayName, String phone) {
		this.userId = userId;
		this.displayName = displayName;
		this.phone = phone;
	}

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }
	public String getDisplayName() { return displayName; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
}
