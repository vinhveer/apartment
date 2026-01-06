package com.qminh.apartment.features.accounts.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserUpdateReq {
	@Email
	@Size(max = 320)
	private String email;
	@Size(max = 255)
	private String displayName;
	@Size(max = 255)
	private String fullName;
	@Size(max = 50)
	private String phone;

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getDisplayName() { return displayName; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
}

