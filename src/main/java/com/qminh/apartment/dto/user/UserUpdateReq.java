package com.qminh.apartment.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserUpdateReq {
	@NotBlank
	@Email
	@Size(max = 320)
	private String email;
	@Size(max = 255)
	private String displayName;

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getDisplayName() { return displayName; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
}


