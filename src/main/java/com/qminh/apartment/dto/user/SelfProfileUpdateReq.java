package com.qminh.apartment.dto.user;

import jakarta.validation.constraints.Size;

public class SelfProfileUpdateReq {
	@Size(max = 255)
	private String displayName;
	@Size(max = 255)
	private String fullName;
	@Size(max = 50)
	private String phone;

	public String getDisplayName() { return displayName; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
}
